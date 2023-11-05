package com.tomato.search.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 图片
 */
@Data
public class Video implements Serializable {
    private String title;
    private String url;
    private String VUrl;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
