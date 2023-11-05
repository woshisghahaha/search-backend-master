package com.tomato.search.service;

import com.tomato.search.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.search.model.entity.User;

/**
 * 帖子点赞服务
 *
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
