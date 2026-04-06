package com.testdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminOverviewVO {
    private long totalGames;
    private long totalUsers;
    private long enabledUsers;
    private long totalRoles;
    private long totalMenus;
    private long activeRateLimitRules;
}
