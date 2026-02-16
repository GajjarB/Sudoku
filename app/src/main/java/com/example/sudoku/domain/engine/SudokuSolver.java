package com.example.sudoku.domain.engine;

/**
 * Solves Sudoku puzzles using backtracking.
 * Used for:
 * - Verifying puzzles have unique solutions
 * - Providing hints to players
 * - Checking if completed puzzle is correct
 */
public class SudokuSolver {
    
    /**
     * Solve a Sudoku puzzle (modifies grid in place).
     * 
     * @param grid 9x9 grid (0 = empty)
     * @return true if solution found, false if unsolvable
     */
    public boolean solve(int[][] grid) {
        return solveBacktrack(grid, 0, 0);
    }
    
    /**
     * Check if puzzle has exactly one unique solution.
     * Critical for puzzle generation!
     */
    public boolean hasUniqueSolution(int[][] grid) {
        int[][] copy1 = copyGrid(grid);
        int[][] copy2 = copyGrid(grid);
        
        // Try to find two different solutions
        if (!solveBacktrack(copy1, 0, 0)) {
            return false; // No solution at all
        }
        
        // Try to find a different solution
        int solutionCount = countSolutions(grid, 0, 0, 2);
        return solutionCount == 1;
    }
    
    /**
     * Count number of solutions (stops at maxCount for efficiency).
     */
    private int countSolutions(int[][] grid, int row, int col, int maxCount) {
        if (col == 9) {
            row++;
            col = 0;
        }
        
        if (row == 9) {
            return 1; // Found one complete solution
        }
        
        if (grid[row][col] != 0) {
            return countSolutions(grid, row, col + 1, maxCount);
        }
        
        int count = 0;
        for (int num = 1; num <= 9 && count < maxCount; num++) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                count += countSolutions(grid, row, col + 1, maxCount - count);
                grid[row][col] = 0;
            }
        }
        
        return count;
    }
    
    /**
     * Backtracking solver.
     */
    private boolean solveBacktrack(int[][] grid, int row, int col) {
        if (col == 9) {
            row++;
            col = 0;
        }
        
        if (row == 9) {
            return true; // Successfully filled grid
        }
        
        if (grid[row][col] != 0) {
            return solveBacktrack(grid, row, col + 1);
        }
        
        for (int num = 1; num <= 9; num++) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                
                if (solveBacktrack(grid, row, col + 1)) {
                    return true;
                }
                
                grid[row][col] = 0;
            }
        }
        
        return false;
    }
    
    /**
     * Check if number placement is valid.
     */
    private boolean isValid(int[][] grid, int row, int col, int num) {
        // Check row
        for (int c = 0; c < 9; c++) {
            if (grid[row][c] == num) return false;
        }
        
        // Check column
        for (int r = 0; r < 9; r++) {
            if (grid[r][col] == num) return false;
        }
        
        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == num) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Deep copy grid.
     */
    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 9);
        }
        return copy;
    }
    
    /**
     * Convert string to 2D array.
     */
    public static int[][] stringToGrid(String s) {
        int[][] grid = new int[9][9];
        for (int i = 0; i < 81; i++) {
            grid[i / 9][i % 9] = s.charAt(i) - '0';
        }
        return grid;
    }
    
    /**
     * Convert 2D array to string.
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
