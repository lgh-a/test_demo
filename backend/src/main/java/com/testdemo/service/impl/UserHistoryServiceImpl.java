package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.entity.GameInfo;
import com.testdemo.entity.UserHistory;
import com.testdemo.mapper.UserHistoryMapper;
import com.testdemo.service.GameService;
import com.testdemo.service.UserHistoryService;
import com.testdemo.vo.HistoryGameVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHistoryServiceImpl implements UserHistoryService {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryServiceImpl.class);

    private final UserHistoryMapper userHistoryMapper;
    private final GameService gameService;

    @Override
    @Transactional
    public void recordPlay(Integer userId, Integer gameId) {
        GameInfo game = gameService.getById(gameId);
        if (game == null) {
            log.warn("Skipped history record because game was not found: userId={}, gameId={}", userId, gameId);
            return;
        }

        UserHistory existing = userHistoryMapper.selectOne(new LambdaQueryWrapper<UserHistory>()
                .eq(UserHistory::getUserId, userId)
                .eq(UserHistory::getGameId, gameId)
                .last("limit 1"));

        if (existing == null) {
            UserHistory history = new UserHistory();
            history.setUserId(userId);
            history.setGameId(gameId);
            history.setLastPlayedAt(LocalDateTime.now());
            userHistoryMapper.insert(history);
            log.info("Recorded new play history: userId={}, gameId={}", userId, gameId);
            return;
        }

        UserHistory history = new UserHistory();
        history.setId(existing.getId());
        history.setLastPlayedAt(LocalDateTime.now());
        userHistoryMapper.updateById(history);
        log.info("Updated play history timestamp: userId={}, gameId={}", userId, gameId);
    }

    @Override
    public List<HistoryGameVO> listRecentHistory(Integer userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return userHistoryMapper.selectRecentHistory(userId, safeLimit);
    }
}
