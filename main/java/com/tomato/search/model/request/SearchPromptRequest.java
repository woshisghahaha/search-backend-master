package com.tomato.search.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author Tomato
 */

@Data
public class SearchPromptRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;
    private String type;

    private static final long serialVersionUID = 1L;
}