package com.testdemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.testdemo.common.Result;
import com.testdemo.service.UserHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history/games")
@RequiredArgsConstructor
@Validated
@Tag(name = "User History", description = "Recent play history APIs")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    @PostMapping("/{gameId}")
    @Operation(summary = "Record play history")
    public Result<?> recordPlay(@Parameter(description = "Game ID") @PathVariable Integer gameId) {
        userHistoryService.recordPlay(StpUtil.getLoginIdAsInt(), gameId);
        return Result.success(null);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent play history")
    public Result<?> recentHistory(
            @Parameter(description = "Max records")
            @RequestParam(defaultValue = "6")
            @Min(value = 1, message = "limit must be greater than 0")
            @Max(value = 20, message = "limit must be less than or equal to 20")
            int limit) {
        return Result.success(userHistoryService.listRecentHistory(StpUtil.getLoginIdAsInt(), limit));
    }
}
