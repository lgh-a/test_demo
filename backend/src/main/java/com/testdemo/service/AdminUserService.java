package com.testdemo.service;

import com.testdemo.common.OperationResult;
import com.testdemo.dto.AdminUserCreateRequest;
import com.testdemo.dto.AdminUserUpdateRequest;
import com.testdemo.vo.AdminUserVO;

import java.util.List;

public interface AdminUserService {

    List<AdminUserVO> listUsers();

    OperationResult<Void> createUser(AdminUserCreateRequest request);

    OperationResult<Void> updateUser(Integer userId, AdminUserUpdateRequest request);

    OperationResult<Void> resetPassword(Integer userId);
}
