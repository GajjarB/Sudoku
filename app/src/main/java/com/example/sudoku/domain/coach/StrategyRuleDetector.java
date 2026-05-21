package com.example.sudoku.domain.coach;

public interface StrategyRuleDetector {
    StrategyResult detect(int[][] grid, int[][] solution);
}
