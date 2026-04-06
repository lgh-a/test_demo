package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AdminGameSaveRequest;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameInfo;
import com.testdemo.entity.GameSeries;
import com.testdemo.mapper.GameConsoleMapper;
import com.testdemo.mapper.GameInfoMapper;
import com.testdemo.mapper.GameSeriesMapper;
import com.testdemo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GameServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameService {

    private final GameConsoleMapper gameConsoleMapper;
    private final GameSeriesMapper gameSeriesMapper;

    @Override
    public IPage<GameInfo> pageGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, String sort, Integer isRecommend) {
        return page(buildPage(current, size), buildPublicQuery(consoleId, seriesId, keyword, sort, isRecommend));
    }

    @Override
    public IPage<GameInfo> pageAdminGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, Integer status, Integer isRecommend) {
        return page(buildPage(current, size), buildAdminQuery(consoleId, seriesId, keyword, status, isRecommend));
    }

    @Override
    @Transactional
    public GameInfo createGame(AdminGameSaveRequest request) {
        validateReferences(request.getConsoleId(), request.getSeriesId());

        GameInfo game = new GameInfo();
        applyRequest(game, request);
        save(game);
        return getById(game.getId());
    }

    @Override
    @Transactional
    public GameInfo updateGame(Integer id, AdminGameSaveRequest request) {
        GameInfo existing = getById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Game not found");
        }

        validateReferences(request.getConsoleId(), request.getSeriesId());
        applyRequest(existing, request);
        updateById(existing);
        return getById(id);
    }

    @Override
    @Transactional
    public boolean deleteGame(Integer id) {
        return removeById(id);
    }

    private LambdaQueryWrapper<GameInfo> buildPublicQuery(
            Integer consoleId,
            Integer seriesId,
            String keyword,
            String sort,
            Integer isRecommend) {
        LambdaQueryWrapper<GameInfo> queryWrapper = buildBaseQuery(consoleId, seriesId, keyword, null, isRecommend);
        queryWrapper.eq(GameInfo::getStatus, 1);
        applySort(queryWrapper, sort, true);
        return queryWrapper;
    }

    private LambdaQueryWrapper<GameInfo> buildAdminQuery(
            Integer consoleId,
            Integer seriesId,
            String keyword,
            Integer status,
            Integer isRecommend) {
        LambdaQueryWrapper<GameInfo> queryWrapper = buildBaseQuery(consoleId, seriesId, keyword, status, isRecommend);
        applySort(queryWrapper, "updated", false);
        return queryWrapper;
    }

    private LambdaQueryWrapper<GameInfo> buildBaseQuery(
            Integer consoleId,
            Integer seriesId,
            String keyword,
            Integer status,
            Integer isRecommend) {
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
        if (status != null) {
            queryWrapper.eq(GameInfo::getStatus, status);
        }
        if (isRecommend != null) {
            queryWrapper.eq(GameInfo::getIsRecommend, isRecommend);
        }
        return queryWrapper;
    }

    private void applySort(LambdaQueryWrapper<GameInfo> queryWrapper, String sort, boolean includeCustomSort) {
        String normalizedSort = sort == null ? "latest" : sort.trim().toLowerCase();
        if (includeCustomSort && !"recommend".equals(normalizedSort)) {
            queryWrapper.orderByAsc(GameInfo::getSort);
        }

        switch (normalizedSort) {
            case "name" -> queryWrapper.orderByAsc(GameInfo::getName).orderByDesc(GameInfo::getId);
            case "updated" -> queryWrapper.orderByDesc(GameInfo::getUpdateTime).orderByDesc(GameInfo::getId);
            case "recommend" -> queryWrapper.orderByAsc(GameInfo::getSort).orderByDesc(GameInfo::getUpdateTime).orderByDesc(GameInfo::getId);
            case "latest" -> queryWrapper.orderByDesc(GameInfo::getCreateTime).orderByDesc(GameInfo::getId);
            default -> queryWrapper.orderByDesc(GameInfo::getId);
        }
    }

    private Page<GameInfo> buildPage(int current, int size) {
        int safeCurrent = Math.max(current, 1);
        int safeSize = Math.max(size, 1);
        return new Page<>(safeCurrent, safeSize);
    }

    private void validateReferences(Integer consoleId, Integer seriesId) {
        GameConsole console = gameConsoleMapper.selectById(consoleId);
        if (console == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Console not found");
        }

        if (seriesId != null) {
            GameSeries series = gameSeriesMapper.selectById(seriesId);
            if (series == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Series not found");
            }
        }
    }

    private void applyRequest(GameInfo game, AdminGameSaveRequest request) {
        game.setName(request.getName().trim());
        game.setDescription(trimToNull(request.getDescription()));
        game.setRomUrl(request.getRomUrl().trim());
        game.setThumbUrl(trimToNull(request.getThumbUrl()));
        game.setConsoleId(request.getConsoleId());
        game.setSeriesId(request.getSeriesId());
        game.setLanguage(normalizeText(request.getLanguage(), "Unknown"));
        game.setRegion(normalizeText(request.getRegion(), "Unknown"));
        game.setGenre(trimToNull(request.getGenre()));
        game.setStatus(normalizeFlag(request.getStatus(), 1));
        game.setIsRecommend(normalizeFlag(request.getIsRecommend(), 0));
        game.setSort(normalizeNumber(request.getSort(), 0));
    }

    private String normalizeText(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Integer normalizeFlag(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer normalizeNumber(Integer value, int defaultValue) {
        return value == null ? defaultValue : Math.max(value, 0);
    }
}
