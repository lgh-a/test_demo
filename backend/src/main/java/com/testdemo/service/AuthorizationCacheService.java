package com.testdemo.service;

import java.util.Collection;

public interface AuthorizationCacheService {

    void clearUserAuthorization(Integer userId);

    void clearUsersAuthorization(Collection<Integer> userIds);

    void clearRoleAuthorization(Integer roleId);

    void clearRolesAuthorization(Collection<Integer> roleIds);
}
