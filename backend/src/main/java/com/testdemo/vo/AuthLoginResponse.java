package com.testdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthLoginResponse {
    private String tokenName;
    private String tokenValue;
    private UserInfoVO userInfo;
}
