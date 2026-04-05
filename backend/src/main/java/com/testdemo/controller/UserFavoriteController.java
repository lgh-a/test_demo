package com.testdemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.testdemo.common.Result;
import com.testdemo.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites/games")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Favorites", description = "Favorite game APIs")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping("/{gameId}")
    @Operation(summary = "Add favorite game")
    public Result<?> addFavorite(@Parameter(description = "Game ID") @PathVariable Integer gameId) {
        userFavoriteService.addFavorite(StpUtil.getLoginIdAsInt(), gameId);
        return Result.success(null);
    }

    @DeleteMapping("/{gameId}")
    @Operation(summary = "Remove favorite game")
    public Result<?> removeFavorite(@Parameter(description = "Game ID") @PathVariable Integer gameId) {
        userFavoriteService.removeFavorite(StpUtil.getLoginIdAsInt(), gameId);
        return Result.success(null);
    }

    @GetMapping
    @Operation(summary = "List favorite games")
    public Result<?> listFavorites(
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "limit must be greater than 0")
            @Max(value = 50, message = "limit must be less than or equal to 50")
            int limit) {
        return Result.success(userFavoriteService.listFavorites(StpUtil.getLoginIdAsInt(), limit));
    }

    @GetMapping("/ids")
    @Operation(summary = "List favorite game ids")
    public Result<?> listFavoriteIds() {
        return Result.success(userFavoriteService.listFavoriteGameIds(StpUtil.getLoginIdAsInt()));
    }
}
