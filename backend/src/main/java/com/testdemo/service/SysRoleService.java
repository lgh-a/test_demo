package com.testdemo.service;

import com.testdemo.common.OperationResult;
import com.testdemo.dto.RoleCreateRequest;
import com.testdemo.entity.SysRole;

import java.util.List;

public interface SysRoleService {

    List<SysRole> listRoles();

    OperationResult<Void> createRole(RoleCreateRequest request);

    OperationResult<Void> deleteRole(Integer id);

    List<Integer> getRoleMenuIds(Integer roleId);

    OperationResult<Void> assignRoleMenus(Integer roleId, List<Integer> menuIds);
}
