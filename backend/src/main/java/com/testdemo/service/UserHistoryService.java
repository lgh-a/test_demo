package com.testdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testdemo.entity.UserHistory;
import com.testdemo.vo.HistoryGameVO;

import java.util.List;

public interface UserHistoryService extends IService<UserHistory> {
    void recordPlay(Integer userId, Integer gameId);

    List<HistoryGameVO> listRecentHistory(Integer userId, int limit);
}
