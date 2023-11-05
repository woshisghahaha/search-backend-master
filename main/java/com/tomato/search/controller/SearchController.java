package com.tomato.search.controller;

import com.tomato.search.common.BaseResponse;
import com.tomato.search.common.ResultUtils;
import com.tomato.search.manager.SearchFacade;
import com.tomato.search.model.request.SearchAllRequest;
import com.tomato.search.model.request.SearchPromptRequest;
import com.tomato.search.model.vo.SearchAllVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 图片接口
 *
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchAllVO> searchAll(@RequestBody SearchAllRequest searchAllRequest) {
        return ResultUtils.success(searchFacade.searchAll(searchAllRequest));
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/getSearchPrompt")
    public BaseResponse getSearchPrompt(SearchPromptRequest searchPromptRequest){

        List<String> searchPrompt = searchFacade.getSearchPrompt(searchPromptRequest);

        return ResultUtils.success(searchPrompt);
    }
}
