package com.example.sudoku.domain.models;

import java.util.List;

/**
 * Represents the 9x9 Sudoku grid.
 */
public class Grid {
    private final Cell[][] cells;

    public Grid(int[][] currentGrid, boolean[][] lockedState) {
        cells = new Cell[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells[r][c] = new Cell(r, c, currentGrid[r][c], lockedState[r][c]);
            }
        }
    }

    public Cell getCell(int row, int col) {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) return null;
        return cells[row][col];
    }
    
    public int[][] toIntArray() {
        int[][] grid = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                grid[r][c] = cells[r][c].getValue();
            }
        }
        return grid;
    }
}
