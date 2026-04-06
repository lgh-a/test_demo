package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.RoleCreateRequest;
import com.testdemo.entity.SysRole;

import java.util.List;

public interface SysRoleService {

    List<SysRole> listRoles();

    IPage<SysRole> pageRoles(int current, int size, String keyword);

    OperationResult<Void> createRole(RoleCreateRequest request);

    OperationResult<Void> deleteRole(Integer id);

    List<Integer> getRoleMenuIds(Integer roleId);

    OperationResult<Void> assignRoleMenus(Integer roleId, List<Integer> menuIds);
}
