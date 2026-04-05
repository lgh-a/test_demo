package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.UserFavorite;
import com.testdemo.vo.FavoriteGameVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    @Select("""
            SELECT
                g.id,
                g.name,
                g.description,
                g.rom_url,
                g.thumb_url,
                g.console_id,
                g.series_id,
                g.language,
                g.region,
                g.genre,
                g.create_time,
                g.update_time,
                f.create_time AS favorited_at
            FROM user_favorite f
            JOIN game_info g ON g.id = f.game_id
            WHERE f.user_id = #{userId}
            ORDER BY f.create_time DESC
            LIMIT #{limit}
            """)
    List<FavoriteGameVO> selectFavoriteGames(@Param("userId") Integer userId, @Param("limit") int limit);

    @Select("SELECT game_id FROM user_favorite WHERE user_id = #{userId}")
    List<Integer> selectFavoriteGameIds(@Param("userId") Integer userId);
}
