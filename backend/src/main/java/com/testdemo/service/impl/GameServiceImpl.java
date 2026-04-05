package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.entity.GameInfo;
import com.testdemo.mapper.GameInfoMapper;
import com.testdemo.service.GameService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GameServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameService {

    @Override
    public IPage<GameInfo> pageGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, String sort) {
        Page<GameInfo> page = new Page<>(current, size);
        int safeCurrent = Math.max(current, 1);
        int safeSize = Math.max(size, 1);
        long total = count(buildQuery(consoleId, seriesId, keyword, sort));
        long offset = (long) (safeCurrent - 1) * safeSize;
        LambdaQueryWrapper<GameInfo> listWrapper = buildQuery(consoleId, seriesId, keyword, sort);
        listWrapper.last("LIMIT " + offset + "," + safeSize);

        page.setCurrent(safeCurrent);
        page.setSize(safeSize);
        page.setTotal(total);
        page.setRecords(list(listWrapper));
        return page;
    }

    private LambdaQueryWrapper<GameInfo> buildQuery(Integer consoleId, Integer seriesId, String keyword, String sort) {
        LambdaQueryWrapper<GameInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (consoleId != null) {
            queryWrapper.eq(GameInfo::getConsoleId, consoleId);
        }
        if (seriesId != null) {
            queryWrapper.eq(GameInfo::getSeriesId, seriesId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(GameInfo::getName, keyword.trim());
        }

        String normalizedSort = sort == null ? "latest" : sort.trim().toLowerCase();
        switch (normalizedSort) {
            case "name" -> queryWrapper.orderByAsc(GameInfo::getName).orderByDesc(GameInfo::getId);
            case "updated" -> queryWrapper.orderByDesc(GameInfo::getUpdateTime).orderByDesc(GameInfo::getId);
            case "latest" -> queryWrapper.orderByDesc(GameInfo::getCreateTime).orderByDesc(GameInfo::getId);
            default -> queryWrapper.orderByDesc(GameInfo::getId);
        }
        return queryWrapper;
    }
}
