package com.testdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthUserInfoResponse {
    private UserInfoVO user;
    private List<String> roles;
    private List<String> permissions;
}
