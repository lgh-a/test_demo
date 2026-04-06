package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.MenuSaveRequest;
import com.testdemo.entity.SysMenu;

import java.util.List;

public interface SysMenuService {

    List<SysMenu> listMenus();

    IPage<SysMenu> pageMenus(int current, int size, String keyword);

    List<SysMenu> treeMenus();

    OperationResult<Void> createMenu(MenuSaveRequest request);

    OperationResult<Void> updateMenu(Integer id, MenuSaveRequest request);

    OperationResult<Void> deleteMenu(Integer id);
}
