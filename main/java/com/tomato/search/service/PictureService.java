package com.tomato.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.model.dto.picture.PictureQueryRequest;
import com.tomato.search.model.entity.Picture;

/**
 * 图片服务
 *
 */
public interface PictureService {

    Page<Picture> searchPicture(PictureQueryRequest picQueryRequest);
}
