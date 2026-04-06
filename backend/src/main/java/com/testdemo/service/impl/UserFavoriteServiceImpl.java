package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.entity.GameInfo;
import com.testdemo.entity.UserFavorite;
import com.testdemo.mapper.UserFavoriteMapper;
import com.testdemo.service.GameService;
import com.testdemo.service.UserFavoriteService;
import com.testdemo.vo.FavoriteGameVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private static final Logger log = LoggerFactory.getLogger(UserFavoriteServiceImpl.class);

    private final UserFavoriteMapper userFavoriteMapper;
    private final GameService gameService;

    @Override
    @Transactional
    public boolean addFavorite(Integer userId, Integer gameId) {
        GameInfo game = gameService.getById(gameId);
        if (game == null) {
            log.warn("Rejected favorite add because game was not found: userId={}, gameId={}", userId, gameId);
            return false;
        }

        UserFavorite existing = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getGameId, gameId)
                .last("limit 1"));
        if (existing != null) {
            return true;
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setGameId(gameId);
        userFavoriteMapper.insert(favorite);
        log.info("Added favorite game: userId={}, gameId={}", userId, gameId);
        return true;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Integer userId, Integer gameId) {
        boolean removed = userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getGameId, gameId)) > 0;
        if (removed) {
            log.info("Removed favorite game: userId={}, gameId={}", userId, gameId);
        }
        return removed;
    }

    @Override
    public List<FavoriteGameVO> listFavorites(Integer userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return userFavoriteMapper.selectFavoriteGames(userId, safeLimit);
    }

    @Override
    public List<Integer> listFavoriteGameIds(Integer userId) {
        return userFavoriteMapper.selectFavoriteGameIds(userId);
    }
}
