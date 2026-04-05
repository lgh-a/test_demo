package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.entity.SysUser;
import com.testdemo.mapper.SysUserMapper;
import com.testdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SysUser login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username).last("limit 1");
        SysUser user = this.getOne(wrapper);
        return matchesPassword(user, password) ? user : null;
    }

    @Override
    @Transactional
    public boolean save(SysUser entity) {
        encodePasswordIfNeeded(entity);
        return super.save(entity);
    }

    @Override
    @Transactional
    public boolean updateById(SysUser entity) {
        encodePasswordIfNeeded(entity);
        return super.updateById(entity);
    }

    @Override
    @Transactional
    public boolean saveOrUpdate(SysUser entity) {
        encodePasswordIfNeeded(entity);
        return super.saveOrUpdate(entity);
    }

    @Override
    @Transactional
    public boolean matchesPassword(SysUser user, String rawPassword) {
        if (user == null || user.getPassword() == null || rawPassword == null) {
            return false;
        }

        String storedPassword = user.getPassword();
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        super.updateById(user);
        return true;
    }

    @Override
    @Transactional
    public boolean updatePassword(Integer userId, String rawPassword) {
        if (userId == null || !StringUtils.hasText(rawPassword)) {
            return false;
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(rawPassword);
        return this.updateById(user);
    }

    private void encodePasswordIfNeeded(SysUser user) {
        if (user == null || !StringUtils.hasText(user.getPassword())) {
            return;
        }
        if (!isBcryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}
