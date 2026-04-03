package com.testdemo.controller;

import com.testdemo.annotation.DynamicRateLimit;
import com.testdemo.annotation.LimitType;
import com.testdemo.config.RateLimitRuleManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "限流示例", description = "双策略动态限流演示接口")
public class RateLimitDemoController {

    @Autowired
    private RateLimitRuleManager ruleManager;

    @GetMapping("/sendSms")
    @DynamicRateLimit(
        configKey = "sms.daily.limit",
        keyPrefix = "limit:daily:sms:",
        dynamicKey = "#phone",
        limitType = LimitType.FIXED_WINDOW_DAILY,
        capacity = 5,
        message = "今日短信发送次数已达上限"
    )
    @Operation(summary = "发送短信", description = "演示每日固定窗口限流策略，默认每天最多5次")
    public String sendSms(@Parameter(description = "手机号") @RequestParam String phone) {
        return "短信发送成功";
    }

    @PostMapping("/admin/updateRule")
    @Operation(summary = "更新限流规则", description = "动态更新限流规则配置，reset=true时递增版本号实现计数空间切换")
    public String updateRule(
            @Parameter(description = "规则Key") @RequestParam String configKey,
            @Parameter(description = "容量/次数") @RequestParam Integer capacity,
            @Parameter(description = "速率") @RequestParam(required = false) Integer rate,
            @Parameter(description = "true时递增版本号，等效于清零当天计数") @RequestParam(required = false, defaultValue = "false") Boolean reset) {
        ruleManager.saveRule(configKey, capacity, rate, reset);
        return "规则动态更新成功";
    }
}