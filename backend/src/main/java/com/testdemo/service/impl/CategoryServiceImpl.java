package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AdminConsoleSaveRequest;
import com.testdemo.dto.AdminSeriesSaveRequest;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameInfo;
import com.testdemo.entity.GameSeries;
import com.testdemo.mapper.GameConsoleMapper;
import com.testdemo.mapper.GameInfoMapper;
import com.testdemo.mapper.GameSeriesMapper;
import com.testdemo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final GameConsoleMapper consoleMapper;
    private final GameSeriesMapper seriesMapper;
    private final GameInfoMapper gameInfoMapper;

    @Override
    public List<GameConsole> getAllConsoles() {
        return consoleMapper.selectList(new LambdaQueryWrapper<GameConsole>()
                .orderByAsc(GameConsole::getName)
                .orderByAsc(GameConsole::getId));
    }

    @Override
    public List<GameSeries> getAllSeries() {
        return seriesMapper.selectList(new LambdaQueryWrapper<GameSeries>()
                .orderByAsc(GameSeries::getName)
                .orderByAsc(GameSeries::getId));
    }

    @Override
    public IPage<GameConsole> pageAdminConsoles(int current, int size, String keyword) {
        LambdaQueryWrapper<GameConsole> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like(GameConsole::getName, normalizedKeyword)
                    .or()
                    .like(GameConsole::getFullName, normalizedKeyword));
        }
        queryWrapper.orderByAsc(GameConsole::getName).orderByAsc(GameConsole::getId);
        return consoleMapper.selectPage(buildPage(current, size), queryWrapper);
    }

    @Override
    public IPage<GameSeries> pageAdminSeries(int current, int size, String keyword) {
        LambdaQueryWrapper<GameSeries> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(GameSeries::getName, keyword.trim());
        }
        queryWrapper.orderByAsc(GameSeries::getName).orderByAsc(GameSeries::getId);
        return seriesMapper.selectPage(buildPage(current, size), queryWrapper);
    }

    @Override
    @Transactional
    public GameConsole createConsole(AdminConsoleSaveRequest request) {
        ensureConsoleNameUnique(request.getName(), null);

        GameConsole console = new GameConsole();
        applyConsoleRequest(console, request);
        consoleMapper.insert(console);
        return consoleMapper.selectById(console.getId());
    }

    @Override
    @Transactional
    public GameConsole updateConsole(Integer id, AdminConsoleSaveRequest request) {
        GameConsole console = consoleMapper.selectById(id);
        if (console == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Console not found");
        }

        ensureConsoleNameUnique(request.getName(), id);
        applyConsoleRequest(console, request);
        consoleMapper.updateById(console);
        return consoleMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteConsole(Integer id) {
        GameConsole console = consoleMapper.selectById(id);
        if (console == null) {
            return false;
        }

        long referencedCount = gameInfoMapper.selectCount(new LambdaQueryWrapper<GameInfo>()
                .eq(GameInfo::getConsoleId, id));
        if (referencedCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Console is referenced by existing games");
        }

        return consoleMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public GameSeries createSeries(AdminSeriesSaveRequest request) {
        ensureSeriesNameUnique(request.getName(), null);

        GameSeries series = new GameSeries();
        applySeriesRequest(series, request);
        seriesMapper.insert(series);
        return seriesMapper.selectById(series.getId());
    }

    @Override
    @Transactional
    public GameSeries updateSeries(Integer id, AdminSeriesSaveRequest request) {
        GameSeries series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Series not found");
        }

        ensureSeriesNameUnique(request.getName(), id);
        applySeriesRequest(series, request);
        seriesMapper.updateById(series);
        return seriesMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteSeries(Integer id) {
        GameSeries series = seriesMapper.selectById(id);
        if (series == null) {
            return false;
        }

        long referencedCount = gameInfoMapper.selectCount(new LambdaQueryWrapper<GameInfo>()
                .eq(GameInfo::getSeriesId, id));
        if (referencedCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Series is referenced by existing games");
        }

        return seriesMapper.deleteById(id) > 0;
    }

    private <T> Page<T> buildPage(int current, int size) {
        int safeCurrent = Math.max(current, 1);
        int safeSize = Math.max(size, 1);
        return new Page<>(safeCurrent, safeSize);
    }

    private void ensureConsoleNameUnique(String name, Integer excludeId) {
        LambdaQueryWrapper<GameConsole> queryWrapper = new LambdaQueryWrapper<GameConsole>()
                .eq(GameConsole::getName, name.trim());
        if (excludeId != null) {
            queryWrapper.ne(GameConsole::getId, excludeId);
        }
        if (consoleMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Console name already exists");
        }
    }

    private void ensureSeriesNameUnique(String name, Integer excludeId) {
        LambdaQueryWrapper<GameSeries> queryWrapper = new LambdaQueryWrapper<GameSeries>()
                .eq(GameSeries::getName, name.trim());
        if (excludeId != null) {
            queryWrapper.ne(GameSeries::getId, excludeId);
        }
        if (seriesMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Series name already exists");
        }
    }

    private void applyConsoleRequest(GameConsole console, AdminConsoleSaveRequest request) {
        console.setName(request.getName().trim());
        console.setFullName(request.getFullName().trim());
        console.setIconUrl(trimToNull(request.getIconUrl()));
    }

    private void applySeriesRequest(GameSeries series, AdminSeriesSaveRequest request) {
        series.setName(request.getName().trim());
        series.setIconUrl(trimToNull(request.getIconUrl()));
        series.setDescription(trimToNull(request.getDescription()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
