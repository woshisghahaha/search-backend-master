package com.tomato.search.model.vo;

import com.tomato.search.model.entity.Picture;
import lombok.Data;

import java.util.List;

@Data
public class SearchAllVO {
    private List<UserVO> userList;
    private List<PostVO> postList;
    private List<Picture> pictureList;
    private List<?>dataList;//只请求其中一个数据源时可以用dataList
    private static final long serialVersionUID = 1L;
}