package com.testdemo.service.impl;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameSeries;
import com.testdemo.mapper.GameConsoleMapper;
import com.testdemo.mapper.GameSeriesMapper;
import com.testdemo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final GameConsoleMapper consoleMapper;
    private final GameSeriesMapper seriesMapper;

    @Override
    public List<GameConsole> getAllConsoles() {
        return consoleMapper.selectList(null);
    }

    @Override
    public List<GameSeries> getAllSeries() {
        return seriesMapper.selectList(null);
    }
}
