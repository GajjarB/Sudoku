package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class HiddenSingleDetector implements StrategyRuleDetector {
    @Override
    public StrategyResult detect(int[][] grid, int[][] solution) {
        StrategyResult rowResult = scanRows(grid);
        if (rowResult.found) return rowResult;

        StrategyResult columnResult = scanColumns(grid);
        if (columnResult.found) return columnResult;

        return scanBoxes(grid);
    }

    private StrategyResult scanRows(int[][] grid) {
        for (int row = 0; row < 9; row++) {
            for (int value = 1; value <= 9; value++) {
                List<CellPosition> cells = new ArrayList<>();
                for (int col = 0; col < 9; col++) {
                    if (grid[row][col] == 0 && GameValidator.getCandidates(grid, row, col).contains(value)) {
                        cells.add(new CellPosition(row, col));
                    }
                }
                if (cells.size() == 1) {
                    CellPosition target = cells.get(0);
                    return result(target, value, rowScope(row), "row");
                }
            }
        }
        return StrategyResult.none();
    }

    private StrategyResult scanColumns(int[][] grid) {
        for (int col = 0; col < 9; col++) {
            for (int value = 1; value <= 9; value++) {
                List<CellPosition> cells = new ArrayList<>();
                for (int row = 0; row < 9; row++) {
                    if (grid[row][col] == 0 && GameValidator.getCandidates(grid, row, col).contains(value)) {
                        cells.add(new CellPosition(row, col));
                    }
                }
                if (cells.size() == 1) {
                    CellPosition target = cells.get(0);
                    return result(target, value, columnScope(col), "column");
                }
            }
        }
        return StrategyResult.none();
    }

    private StrategyResult scanBoxes(int[][] grid) {
        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {
                for (int value = 1; value <= 9; value++) {
                    List<CellPosition> cells = new ArrayList<>();
                    for (int row = boxRow; row < boxRow + 3; row++) {
                        for (int col = boxCol; col < boxCol + 3; col++) {
                            if (grid[row][col] == 0 && GameValidator.getCandidates(grid, row, col).contains(value)) {
                                cells.add(new CellPosition(row, col));
                            }
                        }
                    }
                    if (cells.size() == 1) {
                        CellPosition target = cells.get(0);
                        return result(target, value, boxScope(boxRow, boxCol), "box");
                    }
                }
            }
        }
        return StrategyResult.none();
    }

    private StrategyResult result(CellPosition target, int value, List<CellPosition> highlights, String unitName) {
        return StrategyResult.found(
                Constants.STRATEGY_HIDDEN_SINGLE,
                target.row,
                target.col,
                value,
                highlights,
                "Number " + value + " has only one possible place in this " + unitName + ".",
                "This is a Hidden Single: other empty cells in the " + unitName + " cannot legally hold " + value + ".");
    }

    private List<CellPosition> rowScope(int row) {
        List<CellPosition> cells = new ArrayList<>();
        for (int col = 0; col < 9; col++) cells.add(new CellPosition(row, col));
        return cells;
    }

    private List<CellPosition> columnScope(int col) {
        List<CellPosition> cells = new ArrayList<>();
        for (int row = 0; row < 9; row++) cells.add(new CellPosition(row, col));
        return cells;
    }

    private List<CellPosition> boxScope(int boxRow, int boxCol) {
        List<CellPosition> cells = new ArrayList<>();
        for (int row = boxRow; row < boxRow + 3; row++) {
            for (int col = boxCol; col < boxCol + 3; col++) {
                cells.add(new CellPosition(row, col));
            }
        }
        return cells;
    }
}
