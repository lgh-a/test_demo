package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.testdemo.common.Result;
import com.testdemo.entity.SysMenu;
import com.testdemo.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SysMenuController {

    private final SysMenuMapper sysMenuMapper;

    @GetMapping("/list")
    @SaCheckPermission("sys:menu:list")
    public Result<?> list() {
        return Result.success(sysMenuMapper.selectList(null));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:menu:add")
    public Result<?> add(@RequestBody SysMenu menu) {
        sysMenuMapper.insert(menu);
        return Result.success("Menu added successfully");
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sys:menu:update")
    public Result<?> update(@PathVariable Integer id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuMapper.updateById(menu);
        return Result.success("Menu updated successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:menu:delete")
    public Result<?> delete(@PathVariable Integer id) {
        sysMenuMapper.deleteById(id);
        return Result.success("Menu deleted successfully");
    }
}
