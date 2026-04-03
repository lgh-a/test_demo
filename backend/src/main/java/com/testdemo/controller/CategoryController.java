package com.testdemo.controller;
import com.testdemo.common.Result;
import com.testdemo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "分类查询", description = "游戏主机和系列分类查询")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/consoles")
    @Operation(summary = "获取所有主机", description = "查询所有游戏主机分类")
    public Result<?> getConsoles() {
        return Result.success(categoryService.getAllConsoles());
    }

    @GetMapping("/series")
    @Operation(summary = "获取所有系列", description = "查询所有游戏系列分类")
    public Result<?> getSeries() {
        return Result.success(categoryService.getAllSeries());
    }
}
