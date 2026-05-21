package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.utils.Constants;
import java.util.Arrays;
import java.util.List;

public class NakedSingleDetector implements StrategyRuleDetector {
    @Override
    public StrategyResult detect(int[][] grid, int[][] solution) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (grid[row][col] != 0) continue;
                List<Integer> candidates = GameValidator.getCandidates(grid, row, col);
                if (candidates.size() == 1) {
                    int value = candidates.get(0);
                    return StrategyResult.found(
                            Constants.STRATEGY_NAKED_SINGLE,
                            row,
                            col,
                            value,
                            Arrays.asList(new CellPosition(row, col)),
                            "Only one number can fit in this cell.",
                            "This cell has one legal candidate after checking its row, column, and box.");
                }
            }
        }
        return StrategyResult.none();
    }
}
