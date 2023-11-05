package com.tomato.search.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.model.dto.post.PostQueryRequest;
import com.tomato.search.model.entity.Post;
import com.tomato.search.model.vo.PostVO;
import com.tomato.search.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 帖子服务实现
 *
 */
@Service
@Slf4j
public class PostDataSource implements DataSource<PostVO> {

    @Resource
    private PostService postService;
    /**
     * 适配器模式
     * 假如原本的数据源已经支持搜索，那么就要把它适配我们的接口
     * @param searchText 搜索词
     * @param pageNum 当前页
     * @param pageSize 一页多少数据
     * @return 返回帖子列表
     */
    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(pageSize);
        ServletRequestAttributes servletRequestAttributes =  (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        //使用ES
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postPage, request);
    }

    @Override
    public List<String> getSearchPrompt(String keyword) {
        return Collections.emptyList();
    }
}




