package com.tomato.search.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomato.search.common.ErrorCode;
import com.tomato.search.exception.BusinessException;
import com.tomato.search.model.dto.picture.PictureQueryRequest;
import com.tomato.search.model.entity.Picture;
import com.tomato.search.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现类
 *
 */
@Service
public class PictureDataSource implements DataSource<Picture> {
    @Resource
    private PictureService pictureService;
    /**
     * 适配器模式
     * 假如原本的数据源已经支持搜索，那么就要把它适配我们的接口
     * @param searchText 搜索词
     * @param pageNum 当前页
     * @param pageSize 一页多少数据
     * @return 返回图片列表
     */
    @Override
    public Page<Picture>  doSearch(String searchText, long pageNum, long pageSize) {
        PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
        pictureQueryRequest.setSearchText(searchText);
        pictureQueryRequest.setCurrent(pageNum);
        pictureQueryRequest.setPageSize(pageSize);
        return pictureService.searchPicture(pictureQueryRequest);
    }

    @Override
    public List<String> getSearchPrompt(String keyword) {
        return Collections.emptyList();
    }
}