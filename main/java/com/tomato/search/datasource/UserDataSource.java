package com.tomato.search.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.model.dto.user.UserQueryRequest;
import com.tomato.search.model.vo.UserVO;
import com.tomato.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 用户服务实现
 *
 */
@Service
@Slf4j
public class UserDataSource implements DataSource<UserVO> {

    @Resource
    private UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setCurrent(pageNum);
        userQueryRequest.setPageSize(pageSize);
        return userService.listUserVOByPage(userQueryRequest);
    }

    @Override
    public List<String> getSearchPrompt(String keyword) {
        return Collections.emptyList();
    }
}
