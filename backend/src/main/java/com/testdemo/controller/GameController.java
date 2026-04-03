package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.Result;
import com.testdemo.entity.GameInfo;
import com.testdemo.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "游戏服务", description = "游戏列表查询和详情获取")
public class GameController {
    private final GameService gameService;

    @GetMapping("/list")
    @Operation(summary = "获取游戏列表", description = "支持按主机、系列、关键词筛选")
    public Result<?> listGames(
            @Parameter(description = "主机ID") @RequestParam(required = false) Integer consoleId,
            @Parameter(description = "系列ID") @RequestParam(required = false) Integer seriesId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
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
    @Operation(summary = "获取游戏详情", description = "根据ID获取游戏详细信息，需要game:play权限")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "游戏不存在")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无game:play权限")
    public Result<?> getGame(@Parameter(description = "游戏ID") @PathVariable Integer id) {
        GameInfo game = gameService.getById(id);
        if (game == null) {
            return Result.error("Game not found");
        }
        return Result.success(game);
    }
}
