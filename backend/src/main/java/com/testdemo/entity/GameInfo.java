package com.testdemo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game_info")
public class GameInfo {
    @TableId(type = IdType.AUTO)
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
}
