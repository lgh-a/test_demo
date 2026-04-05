package com.testdemo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteGameVO {
    private Integer id;
    private String name;
    private String description;
    private String romUrl;
    private String thumbUrl;
    private Integer consoleId;
    private Integer seriesId;
    private String language;
    private String region;
    private String genre;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime favoritedAt;
}
