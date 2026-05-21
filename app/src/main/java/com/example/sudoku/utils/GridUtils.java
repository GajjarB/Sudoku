package com.example.sudoku.utils;

/**
 * Utility class for Grid operations.
 */
public class GridUtils {

    /**
     * Parses a string representation of the grid (81 chars) into a 9x9 int array.
     * '0' or '.' represents an empty cell.
     */
    public static int[][] parseGrid(String gridString) {
        int[][] grid = new int[9][9];
        if (gridString == null || gridString.length() != 81) {
            return grid;
        }
        
        for (int i = 0; i < 81; i++) {
            char c = gridString.charAt(i);
            if (c >= '1' && c <= '9') {
                grid[i / 9][i % 9] = c - '0';
            } else if (c == '0' || c == '.') {
                grid[i / 9][i % 9] = 0;
            } else {
                return new int[9][9];
            }
        }
        return grid;
    }
}
