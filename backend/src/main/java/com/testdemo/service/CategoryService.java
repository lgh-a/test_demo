package com.testdemo.service;
import com.testdemo.entity.GameConsole;
import com.testdemo.entity.GameSeries;
import java.util.List;

public interface CategoryService {
    List<GameConsole> getAllConsoles();
    List<GameSeries> getAllSeries();
}
