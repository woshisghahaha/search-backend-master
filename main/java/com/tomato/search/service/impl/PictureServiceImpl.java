package com.tomato.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.common.ErrorCode;
import com.tomato.search.exception.BusinessException;
import com.tomato.search.model.dto.picture.PictureQueryRequest;
import com.tomato.search.model.entity.Picture;
import com.tomato.search.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图片服务实现类
 *
 */
@Service
public class PictureServiceImpl implements PictureService {
    /**
     * 爬取图片数据
     * @param picQueryRequest
     * @return
     */
    @Override
    public Page<Picture> searchPicture(PictureQueryRequest picQueryRequest) {
        String searchText = picQueryRequest.getSearchText();
        long current = picQueryRequest.getCurrent();
        long pageSize = picQueryRequest.getPageSize();
        long index = (current-1)*pageSize+1;//当前页是2，一页8条，那么就要从（2-1）*8条数据开始显示
        // url里的first参数是指定从第几张图片开始显示,使用占位符动态指定，要求url里本身不带%才能用String.format
        // 如果想排序的话，不用自己实现
        // 直接根据bing排序后的url抓即可。无非是下面多几个参数，在前端没传这几个参数的时候用空串应该就行了
        String url = String.format("https://www.bing.com/images/search?q=%s&first=%s",searchText,index);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片数据获取异常");
        }
        //下面应该是不用再判断抓到的是否为null，抓不到直接抛异常了...
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures=new ArrayList<>();
        for (Element element : elements) {
            // 获取图片地址
            // 通过CSS选择器选取所有具有类名为 "iusc" 的元素。返回的是一个元素列表
            // .get(0) 这个方法获取列表中的第一个元素。
            // .attr("m") 这个方法获取选定元素的属性值
            // 这个需要在浏览器控制台 元素栏下对着看才找得到类名，找到之后可以先调试
            // 看看里面有什么值，再从里面取元素的属性值
            String m = element.select(".iusc").get(0).attr("m");
            Map<String,Object> map= JSONUtil.toBean(m,Map.class);//调试可以看到这个m是个JSON，里面的murl字段才是我们要的图片链接
            String murl = (String)map.get("murl");
            // 获取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            String sourceUrl = (String) map.get("purl");
            //封装存入，其实用有参构造也行
            Picture Cachepic=new Picture();
            Cachepic.setTitle(title);
            Cachepic.setUrl(murl);
            Cachepic.setSourceUrl(sourceUrl);
            pictures.add(Cachepic);
            /*if(isUrlAccessible(murl)){
                pictures.add(Cachepic);
            }*/
            if(pictures.size()>=pageSize)break;//查询pageSize数据就够了
        }
        // 过滤掉URL、title为空的数据
        List<Picture> collect = pictures.stream()
                .filter(picture -> StringUtils.isNotBlank(picture.getTitle()) || StringUtils.isNotBlank(picture.getUrl()))
                .collect(Collectors.toList());

        // 总共爬了多少pic，这个获取不到，就不管total参数了
        Page<Picture>picturePage=new Page<>(current,pageSize);
        picturePage.setRecords(collect);
        return picturePage;
    }
    //检查图片URL是否可访问
    private boolean isUrlAccessible(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (responseCode == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }
}
