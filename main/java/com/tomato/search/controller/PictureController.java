package com.tomato.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.common.BaseResponse;
import com.tomato.search.common.ErrorCode;
import com.tomato.search.common.ResultUtils;
import com.tomato.search.exception.ThrowUtils;
import com.tomato.search.model.dto.picture.PictureQueryRequest;
import com.tomato.search.model.entity.Picture;
import com.tomato.search.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片接口
 *
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    /**
     * 分页获取列表（封装类）
     *
     * @param picQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest picQueryRequest,
                                                         HttpServletRequest request) {
        long size = picQueryRequest.getPageSize();
        // 限制爬虫，前端一次最多请求20条数据
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        Page<Picture>pictures = pictureService.searchPicture(picQueryRequest);
        return ResultUtils.success(pictures);
    }


}
