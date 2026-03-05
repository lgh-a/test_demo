package com.testdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testdemo.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
}
