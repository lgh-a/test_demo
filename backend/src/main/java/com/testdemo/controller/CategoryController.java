package com.testdemo.controller;
import com.testdemo.common.Result;
import com.testdemo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/consoles")
    public Result<?> getConsoles() {
        return Result.success(categoryService.getAllConsoles());
    }

    @GetMapping("/series")
    public Result<?> getSeries() {
        return Result.success(categoryService.getAllSeries());
    }
}
