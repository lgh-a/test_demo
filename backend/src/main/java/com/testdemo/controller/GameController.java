package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.entity.GameInfo;
import com.testdemo.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Validated
@Tag(name = "Games", description = "Game list and detail APIs")
public class GameController {

    private final GameService gameService;

    @GetMapping("/list")
    @Operation(summary = "Get game list", description = "Filter by console, series, and keyword")
    public Result<?> listGames(
            @Parameter(description = "Current page")
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "24")
            @Min(value = 1, message = "size must be greater than 0")
            @Max(value = 60, message = "size must be less than or equal to 60")
            int size,
            @Parameter(description = "Console ID") @RequestParam(required = false) Integer consoleId,
            @Parameter(description = "Series ID") @RequestParam(required = false) Integer seriesId,
            @Parameter(description = "Keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Sort mode: latest, updated, name") @RequestParam(defaultValue = "latest") String sort) {
        IPage<GameInfo> page = gameService.pageGames(current, size, consoleId, seriesId, keyword, sort);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("game:play")
    @Operation(summary = "Get game detail", description = "Query game detail by ID")
    @ApiResponse(responseCode = "200", description = "Query successful")
    @ApiResponse(responseCode = "404", description = "Game not found")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing game:play permission")
    public Result<?> getGame(@Parameter(description = "Game ID") @PathVariable Integer id) {
        GameInfo game = gameService.getById(id);
        if (game == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return Result.success(game);
    }
}
