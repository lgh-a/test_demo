package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.testdemo.entity.GameInfo;

public interface GameService extends IService<GameInfo> {
    IPage<GameInfo> pageGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, String sort);
}
