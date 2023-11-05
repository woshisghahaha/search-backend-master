package com.tomato.search.model.request;

import com.tomato.search.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询所有请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
//图片请求继承了分页请求
public class SearchAllRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 搜索类型
     */
    private String type;

    private static final long serialVersionUID = 1L;
}
