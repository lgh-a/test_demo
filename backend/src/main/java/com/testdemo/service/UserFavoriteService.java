package com.testdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testdemo.entity.UserFavorite;
import com.testdemo.vo.FavoriteGameVO;

import java.util.List;

public interface UserFavoriteService extends IService<UserFavorite> {
    boolean addFavorite(Integer userId, Integer gameId);

    boolean removeFavorite(Integer userId, Integer gameId);

    List<FavoriteGameVO> listFavorites(Integer userId, int limit);

    List<Integer> listFavoriteGameIds(Integer userId);
}
