package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AdminConsoleSaveRequest;
import com.testdemo.dto.AdminSeriesSaveRequest;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameSeries;
import com.testdemo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Categories", description = "Admin console and series management")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/consoles/page")
    @SaCheckPermission("console:manage:list")
    @Operation(summary = "Page consoles for admin")
    public Result<?> pageConsoles(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be greater than 0")
            @Max(value = 50, message = "size must be less than or equal to 50")
            int size,
            @RequestParam(required = false) String keyword) {
        IPage<GameConsole> page = categoryService.pageAdminConsoles(current, size, keyword);
        return Result.success(page);
    }

    @GetMapping("/series/page")
    @SaCheckPermission("series:manage:list")
    @Operation(summary = "Page series for admin")
    public Result<?> pageSeries(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be greater than 0")
            @Max(value = 50, message = "size must be less than or equal to 50")
            int size,
            @RequestParam(required = false) String keyword) {
        IPage<GameSeries> page = categoryService.pageAdminSeries(current, size, keyword);
        return Result.success(page);
    }

    @PostMapping("/consoles")
    @SaCheckPermission("console:manage:add")
    @Operation(summary = "Create console")
    public Result<?> createConsole(@Valid @RequestBody AdminConsoleSaveRequest request) {
        return Result.success(categoryService.createConsole(request));
    }

    @PutMapping("/consoles/{id}")
    @SaCheckPermission("console:manage:update")
    @Operation(summary = "Update console")
    public Result<?> updateConsole(@PathVariable Integer id, @Valid @RequestBody AdminConsoleSaveRequest request) {
        return Result.success(categoryService.updateConsole(id, request));
    }

    @DeleteMapping("/consoles/{id}")
    @SaCheckPermission("console:manage:delete")
    @Operation(summary = "Delete console")
    public Result<?> deleteConsole(@PathVariable Integer id) {
        if (!categoryService.deleteConsole(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Console not found");
        }
        return Result.success(null);
    }

    @PostMapping("/series")
    @SaCheckPermission("series:manage:add")
    @Operation(summary = "Create series")
    public Result<?> createSeries(@Valid @RequestBody AdminSeriesSaveRequest request) {
        return Result.success(categoryService.createSeries(request));
    }

    @PutMapping("/series/{id}")
    @SaCheckPermission("series:manage:update")
    @Operation(summary = "Update series")
    public Result<?> updateSeries(@PathVariable Integer id, @Valid @RequestBody AdminSeriesSaveRequest request) {
        return Result.success(categoryService.updateSeries(id, request));
    }

    @DeleteMapping("/series/{id}")
    @SaCheckPermission("series:manage:delete")
    @Operation(summary = "Delete series")
    public Result<?> deleteSeries(@PathVariable Integer id) {
        if (!categoryService.deleteSeries(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Series not found");
        }
        return Result.success(null);
    }
}
