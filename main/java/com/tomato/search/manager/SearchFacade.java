package com.tomato.search.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.common.ErrorCode;
import com.tomato.search.datasource.*;
import com.tomato.search.exception.BusinessException;
import com.tomato.search.model.request.SearchAllRequest;
import com.tomato.search.model.request.SearchPromptRequest;
import com.tomato.search.model.entity.Picture;
import com.tomato.search.model.enums.SearchTypeEnum;
import com.tomato.search.model.vo.PostVO;
import com.tomato.search.model.vo.SearchAllVO;
import com.tomato.search.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 搜索门面
 *
 */
@Slf4j
@Component
public class SearchFacade {
    @Resource
    private PictureDataSource pictureDataSource;
    @Resource
    private PostDataSource postDataSource;
    @Resource
    private UserDataSource userDataSource;
    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchAllVO searchAll(@RequestBody SearchAllRequest searchAllRequest) {

        String type = searchAllRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        //ThrowUtils.throwIf(type == null, ErrorCode.PARAMS_ERROR);
        //不是三种枚举类型，默认全查
        String searchText = searchAllRequest.getSearchText();
        long pageNum = searchAllRequest.getCurrent();
        long pageSize = searchAllRequest.getPageSize();
        if (searchTypeEnum == null) {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //设置为子线程共享
            RequestContextHolder.setRequestAttributes(sra, true);

            CompletableFuture<Page<Picture>> pictureTask =
                    CompletableFuture.supplyAsync(() -> pictureDataSource.doSearch(searchText, pageNum, pageSize));

            CompletableFuture<Page<UserVO>> userTask =
                    CompletableFuture.supplyAsync(() -> userDataSource.doSearch(searchText, pageNum, pageSize));

            CompletableFuture<Page<PostVO>> postTask =
                    CompletableFuture.supplyAsync(() -> postDataSource.doSearch(searchText, pageNum, pageSize));

            CompletableFuture.allOf(userTask, postTask, pictureTask).join(); //等上面三个异步全完成，短板效应，看谁最慢

            try {
                Page<Picture> picturePage = pictureTask.get();
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                SearchAllVO searchAllVO = new SearchAllVO();
                searchAllVO.setUserList(userVOPage.getRecords());
                searchAllVO.setPostList(postVOPage.getRecords());
                searchAllVO.setPictureList(picturePage.getRecords());
                return searchAllVO;
            } catch (Exception e) {
                log.error("searchAll查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "searchAll查询异常");
            }
        } else {
            SearchAllVO searchAllVO = new SearchAllVO();
            DataSource<?>dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?>page = dataSource.doSearch(searchText, pageNum, pageSize);
            searchAllVO.setDataList(page.getRecords());
            return searchAllVO;
        }
    }

    public List<String> getSearchPrompt(SearchPromptRequest searchPromptRequest) {
        String type = searchPromptRequest.getType();//类型
        String keyword = searchPromptRequest.getSearchText();//搜索关键词
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        if (searchTypeEnum != null) {
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            // 搜索建议统一返回字符串，就不需要泛型了
            return dataSource.getSearchPrompt(keyword);

        } else return Collections.emptyList();//不在我们标签里的，就不用提供搜索建议服务
    }
}