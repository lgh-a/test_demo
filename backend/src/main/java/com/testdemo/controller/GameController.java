package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.Result;
import com.testdemo.entity.GameInfo;
import com.testdemo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameController {
    private final GameService gameService;

    @GetMapping("/list")
    public Result<?> listGames(@RequestParam(required = false) Integer consoleId,
            @RequestParam(required = false) Integer seriesId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<GameInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (consoleId != null) {
            queryWrapper.eq(GameInfo::getConsoleId, consoleId);
        }
        if (seriesId != null) {
            queryWrapper.eq(GameInfo::getSeriesId, seriesId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(GameInfo::getName, keyword);
        }
        queryWrapper.orderByDesc(GameInfo::getId);
        return Result.success(gameService.list(queryWrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("game:play")
    public Result<?> getGame(@PathVariable Integer id) {
        GameInfo game = gameService.getById(id);
        if (game == null) {
            return Result.error("Game not found");
        }
        return Result.success(game);
    }
}
