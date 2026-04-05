package com.testdemo.service;

import com.testdemo.vo.HistoryGameVO;

import java.util.List;

public interface UserHistoryService {
    void recordPlay(Integer userId, Integer gameId);

    List<HistoryGameVO> listRecentHistory(Integer userId, int limit);
}
