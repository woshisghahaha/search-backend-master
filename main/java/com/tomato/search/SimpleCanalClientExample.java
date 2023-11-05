package com.tomato.search;

import java.net.InetSocketAddress;
import java.util.List;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.tomato.search.esdao.PostEsDao;
import com.tomato.search.model.dto.post.PostEsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SimpleCanalClientExample {
    @Resource
    private PostEsDao postEsDao;

    private static final ExecutorService HandleCanal_EXECUTOR = Executors.newSingleThreadExecutor();

    // 成员内部类，创建线程的两种方式，继承thread或实现Runnable来重写run函数
    // 该线程在类创建好就要不停的跑，所以用spring的注解@PostConstruct，让类一创建就初始化成员内部类
    // 不能用静态内部类，因为静态内部类只能调用静态成员

   /* @PostConstruct
    public void init() {
        HandleCanal_EXECUTOR.submit(new incSyncMySqlDataToEs());
    }

    private class  incSyncMySqlDataToEs implements Runnable {
        @Override
        // 把incSyncMySqlDataToEs放进run函数体，其他函数添加进此类，即可实现异步不停监控canal变化
        public void run() {}
    }*/

    public  void incSyncMySqlDataToEs() {
        // 创建链接，connector也是canal数据操作客户端
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        int batchSize = 1000;
        try {
            // 链接对应的canal server
            connector.connect();
            // 客户端订阅，重复订阅时会更新对应的filter信息，这里订阅所有库的所有表
            connector.subscribe(".*\\..*");
            // 回滚到未进行 ack 的地方，下次fetch的时候，可以从最后一个没有 ack 的地方开始拿
            connector.rollback();
            while (true) {
                // 尝试拿batchSize条记录，有多少取多少，不会阻塞等待
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                // 消息id
                long batchId = message.getId();
                // 实际获取记录数
                int size = message.getEntries().size();

                List<CanalEntry.Entry> entries = message.getEntries();
                // 如果获取到消息
                if (CollUtil.isNotEmpty(entries)){
                    log.info("共{}条数据变更",size);
                    printEntry(entries);
                }
                connector.ack(batchId); // 提交确认
                // 回滚到未进行 ack 的地方，指定回滚具体的batchId；如果不指定batchId，回滚到未进行ack的地方
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    private void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            // 如果是事务操作，直接忽略。 EntryType常见取值：事务头BEGIN/事务尾END/数据ROWDATA
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage ;
            try {
                // 获取byte数据，并反序列化
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));
            // 如果是ddl或者是查询操作，直接打印sql
            System.out.println(rowChage.getSql() + ";");
            // 如果是删除、更新、新增操作解析出数据
            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    // 删除操作，只有删除前的数据
                    // 不需要处理，因为只有逻辑删除，直接取新增后数据即可

                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    // 新增数据，只有新增后的数据
                    postEsDao.save(saveToES(rowData));
                } else {
                    // 更新数据：获取更新前后内容
                    System.out.println("-------&gt; before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------&gt; after");
                    postEsDao.save(saveToES(rowData));
                }
            }
        }
    }

    private static PostEsDTO saveToES(RowData rowData) {
        //数据改变后，把每条数据取出来存入JSONObject，再利用BeanUtil.copyProperties，就可以快速完成转型！
        JSONObject afterData = new JSONObject();
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            afterData.set(column.getName(), column.getValue());
        }
        printColumn(afterColumnsList);
        return BeanUtil.copyProperties(afterData, PostEsDTO.class);
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

}