package com.testdemo.controller;

import com.testdemo.common.Result;
import com.testdemo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Query", description = "Game console and series query APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/consoles")
    @Operation(summary = "Get all consoles", description = "Query all game consoles")
    public Result<?> getConsoles() {
        return Result.success(categoryService.getAllConsoles());
    }

    @GetMapping("/series")
    @Operation(summary = "Get all series", description = "Query all game series")
    public Result<?> getSeries() {
        return Result.success(categoryService.getAllSeries());
    }
}
