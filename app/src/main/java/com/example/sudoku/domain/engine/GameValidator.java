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
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
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
        if (grid[row][col] != 0) {
            return new ArrayList<>(); // Cell already filled
        }
        
        boolean[] used = new boolean[10]; // 1-9
        
        // Mark used in row
        for (int c = 0; c < 9; c++) {
            used[grid[row][c]] = true;
        }
        
        // Mark used in column
        for (int r = 0; r < 9; r++) {
            used[grid[r][col]] = true;
        }
        
        // Mark used in box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                used[grid[r][c]] = true;
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
        for (int i = 0; i < 81; i++) {
            grid[i / 9][i % 9] = s.charAt(i) - '0';
        }
        return grid;
    }
    
    /**
     * Convert 2D grid to string.
     */
    public static String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                sb.append(grid[r][c]);
            }
        }
        return sb.toString();
    }
}
