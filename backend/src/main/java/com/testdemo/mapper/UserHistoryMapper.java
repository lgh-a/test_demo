package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.UserHistory;
import com.testdemo.vo.HistoryGameVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserHistoryMapper extends BaseMapper<UserHistory> {

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
                h.last_played_at
            FROM user_history h
            JOIN game_info g ON g.id = h.game_id
            WHERE h.user_id = #{userId}
            ORDER BY h.last_played_at DESC
            LIMIT #{limit}
            """)
    List<HistoryGameVO> selectRecentHistory(@Param("userId") Integer userId, @Param("limit") int limit);
}
