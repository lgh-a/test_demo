package com.testdemo.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.entity.GameInfo;
import com.testdemo.mapper.GameInfoMapper;
import com.testdemo.service.GameService;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameService {
}
