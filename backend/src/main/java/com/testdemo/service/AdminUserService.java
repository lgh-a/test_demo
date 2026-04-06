package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.AdminUserCreateRequest;
import com.testdemo.dto.AdminUserUpdateRequest;
import com.testdemo.vo.AdminUserVO;

import java.util.List;

public interface AdminUserService {

    List<AdminUserVO> listUsers();

    IPage<AdminUserVO> pageUsers(int current, int size, String keyword, Integer status);

    OperationResult<Void> createUser(AdminUserCreateRequest request);

    OperationResult<Void> updateUser(Integer userId, AdminUserUpdateRequest request);

    OperationResult<Void> resetPassword(Integer userId);
}
