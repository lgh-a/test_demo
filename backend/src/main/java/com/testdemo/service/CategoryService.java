package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.dto.AdminConsoleSaveRequest;
import com.testdemo.dto.AdminSeriesSaveRequest;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameSeries;
import java.util.List;

public interface CategoryService {
    List<GameConsole> getAllConsoles();
    List<GameSeries> getAllSeries();
    IPage<GameConsole> pageAdminConsoles(int current, int size, String keyword);
    IPage<GameSeries> pageAdminSeries(int current, int size, String keyword);
    GameConsole createConsole(AdminConsoleSaveRequest request);
    GameConsole updateConsole(Integer id, AdminConsoleSaveRequest request);
    boolean deleteConsole(Integer id);
    GameSeries createSeries(AdminSeriesSaveRequest request);
    GameSeries updateSeries(Integer id, AdminSeriesSaveRequest request);
    boolean deleteSeries(Integer id);
}
