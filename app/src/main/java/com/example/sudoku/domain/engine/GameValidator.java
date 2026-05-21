package com.example.sudoku.domain.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates Sudoku moves and solutions in real-time.
 * Critical for instant feedback during gameplay.
 * 
 * Performance: <1ms per validation (must be instant for UX).
 */
public class GameValidator {
    
    /**
     * Check if placing a number at (row, col) is valid.
     * 
     * @param grid Current game grid
     * @param row Row index (0-8)
     * @param col Column index (0-8)
     * @param num Number to place (1-9)
     * @return true if move is valid
     */
    public static boolean isValidMove(int[][] grid, int row, int col, int num) {
        if (!isValidGrid(grid) || !isValidPosition(row, col) || !isValidNumber(num)) {
            return false;
        }

        // Check if cell is already filled
        if (grid[row][col] != 0) {
            return false;
        }
        
        // Check row constraint
        for (int c = 0; c < 9; c++) {
            if (grid[row][c] == num) {
                return false;
            }
        }
        
        // Check column constraint
        for (int r = 0; r < 9; r++) {
            if (grid[r][col] == num) {
                return false;
            }
        }
        
        // Check 3x3 box constraint
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == num) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get list of conflicting cell positions for a move.
     * Used to highlight conflicts in UI.
     * 
     * @return List of [row, col] pairs that conflict
     */
    public static List<int[]> getConflictingCells(int[][] grid, int row, int col, int num) {
        List<int[]> conflicts = new ArrayList<>();
        if (!isValidGrid(grid) || !isValidPosition(row, col) || !isValidNumber(num)) {
            return conflicts;
        }
        
        // Row conflicts
        for (int c = 0; c < 9; c++) {
            if (c != col && grid[row][c] == num) {
                conflicts.add(new int[]{row, c});
            }
        }
        
        // Column conflicts
        for (int r = 0; r < 9; r++) {
            if (r != row && grid[r][col] == num) {
                conflicts.add(new int[]{r, col});
            }
        }
        
        // Box conflicts
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if ((r != row || c != col) && grid[r][c] == num) {
                    conflicts.add(new int[]{r, c});
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Check if the puzzle is completely and correctly solved.
     * 
     * @param grid Current grid state
     * @param solution Correct solution
     * @return true if puzzle matches solution exactly
     */
    public static boolean isCompleteSolution(int[][] grid, int[][] solution) {
        if (!isValidGrid(grid) || !isValidGrid(solution)) {
            return false;
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] != solution[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check if grid is completely filled (may not be correct).
     */
    public static boolean isGridComplete(int[][] grid) {
        if (!isValidGrid(grid)) {
            return false;
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!isValidNumber(grid[r][c])) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Get all possible valid numbers for a cell (candidates).
     * Used for hints and AI strategy detection.
     */
    public static List<Integer> getCandidates(int[][] grid, int row, int col) {
        if (!isValidGrid(grid) || !isValidPosition(row, col)) {
            return new ArrayList<>();
        }
        if (grid[row][col] != 0) {
            return new ArrayList<>(); // Cell already filled
        }
        
        boolean[] used = new boolean[10]; // 1-9
        
        // Mark used in row
        for (int c = 0; c < 9; c++) {
            markUsed(used, grid[row][c]);
        }
        
        // Mark used in column
        for (int r = 0; r < 9; r++) {
            markUsed(used, grid[r][col]);
        }
        
        // Mark used in box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                markUsed(used, grid[r][c]);
            }
        }
        
        List<Integer> candidates = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (!used[i]) {
                candidates.add(i);
            }
        }
        
        return candidates;
    }
    
    /**
     * Convert string to 2D grid.
     */
    public static int[][] stringToGrid(String s) {
        int[][] grid = new int[9][9];
        if (s == null || s.length() != 81) {
            return grid;
        }
        for (int i = 0; i < 81; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                grid[i / 9][i % 9] = c - '0';
            } else if (c == '.') {
                grid[i / 9][i % 9] = 0;
            } else {
                return new int[9][9];
            }
        }
        return grid;
    }
    
    /**
     * Convert 2D grid to string.
     */
    public static String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        if (!isValidGrid(grid)) {
            return "0".repeat(81);
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int value = grid[r][c];
                sb.append(value >= 0 && value <= 9 ? value : 0);
            }
        }
        return sb.toString();
    }

    private static void markUsed(boolean[] used, int value) {
        if (isValidNumber(value)) {
            used[value] = true;
        }
    }

    private static boolean isValidGrid(int[][] grid) {
        if (grid == null || grid.length != 9) {
            return false;
        }
        for (int r = 0; r < 9; r++) {
            if (grid[r] == null || grid[r].length != 9) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9;
    }

    private static boolean isValidNumber(int num) {
        return num >= 1 && num <= 9;
    }
}
