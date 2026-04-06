package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AdminGameSaveRequest;
import com.testdemo.entity.GameInfo;
import com.testdemo.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Games", description = "Admin game management")
public class AdminGameController {

    private final GameService gameService;

    @GetMapping("/page")
    @SaCheckPermission("game:manage:list")
    @Operation(summary = "Page games for admin")
    public Result<?> page(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be greater than 0")
            @Max(value = 50, message = "size must be less than or equal to 50")
            int size,
            @RequestParam(required = false) Integer consoleId,
            @RequestParam(required = false) Integer seriesId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isRecommend) {
        IPage<GameInfo> page = gameService.pageAdminGames(current, size, consoleId, seriesId, keyword, status, isRecommend);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("game:manage:list")
    @Operation(summary = "Get game detail for admin")
    public Result<?> getById(@Parameter(description = "Game ID") @PathVariable Integer id) {
        GameInfo game = gameService.getById(id);
        if (game == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return Result.success(game);
    }

    @PostMapping
    @SaCheckPermission("game:manage:add")
    @Operation(summary = "Create game")
    public Result<?> create(@Valid @RequestBody AdminGameSaveRequest request) {
        GameInfo created = gameService.createGame(request);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("game:manage:update")
    @Operation(summary = "Update game")
    public Result<?> update(@PathVariable Integer id, @Valid @RequestBody AdminGameSaveRequest request) {
        GameInfo updated = gameService.updateGame(id, request);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("game:manage:delete")
    @Operation(summary = "Delete game")
    public Result<?> delete(@PathVariable Integer id) {
        if (!gameService.deleteGame(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return Result.success(null);
    }
}
