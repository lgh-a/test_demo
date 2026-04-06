package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.dto.AdminGameSaveRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.testdemo.entity.GameInfo;

public interface GameService extends IService<GameInfo> {
    IPage<GameInfo> pageGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, String sort, Integer isRecommend);
    IPage<GameInfo> pageAdminGames(int current, int size, Integer consoleId, Integer seriesId, String keyword, Integer status, Integer isRecommend);
    GameInfo createGame(AdminGameSaveRequest request);
    GameInfo updateGame(Integer id, AdminGameSaveRequest request);
    boolean deleteGame(Integer id);
}
