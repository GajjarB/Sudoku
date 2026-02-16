package com.example.sudoku.domain.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates valid Sudoku puzzles with unique solutions.
 * Uses backtracking algorithm with randomization.
 * 
 * Performance: ~50-200ms per puzzle on modern devices.
 */
public class SudokuGenerator {
    
    private Random random;
    private SudokuSolver solver;
    
    public SudokuGenerator() {
        this.random = new Random();
        this.solver = new SudokuSolver();
    }
    
    /**
     * Constructor with seed for reproducible generation (daily challenges).
     */
    public SudokuGenerator(long seed) {
        this.random = new Random(seed);
        this.solver = new SudokuSolver();
    }
    
    /**
     * Generate a complete Sudoku puzzle with specified difficulty.
     * 
     * @param clueCount Number of given clues (22-40)
     *                  22-25 = Expert
     *                  26-31 = Hard
     *                  32-37 = Medium
     *                  38-40 = Easy
     * @return Puzzle object containing puzzle and solution strings
     */
    public Puzzle generate(int clueCount) {
        // Step 1: Generate a complete valid solution
        int[][] solution = generateCompleteSolution();
        
        // Step 2: Create puzzle by removing clues
        int[][] puzzle = createPuzzleFromSolution(solution, clueCount);
        
        // Step 3: Verify unique solution
        if (!solver.hasUniqueSolution(puzzle)) {
            // Rare case: regenerate if solution isn't unique
            return generate(clueCount);
        }
        
        // Convert to strings
        String puzzleStr = gridToString(puzzle);
        String solutionStr = gridToString(solution);
        
        return new Puzzle(puzzleStr, solutionStr, clueCount);
    }
    
    /**
     * Generate a complete valid 9x9 Sudoku solution.
     * Uses backtracking with random number ordering for variety.
     */
    private int[][] generateCompleteSolution() {
        int[][] grid = new int[9][9];
        fillGrid(grid, 0, 0);
        return grid;
    }
    
    /**
     * Recursive backtracking to fill grid with valid solution.
     */
    private boolean fillGrid(int[][] grid, int row, int col) {
        // Move to next row if reached end of current row
        if (col == 9) {
            row++;
            col = 0;
        }
        
        // Base case: grid completely filled
        if (row == 9) {
            return true;
        }
        
        // Try numbers 1-9 in random order
        List<Integer> numbers = getShuffledNumbers();
        
        for (int num : numbers) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                
                if (fillGrid(grid, row, col + 1)) {
                    return true;
                }
                
                grid[row][col] = 0; // Backtrack
            }
        }
        
        return false;
    }
    
    /**
     * Create puzzle by removing clues from solution.
     * Removes symmetrically for aesthetic appeal.
     */
    private int[][] createPuzzleFromSolution(int[][] solution, int clueCount) {
        int[][] puzzle = copyGrid(solution);
        int cellsToRemove = 81 - clueCount;
        
        // Create list of all cell positions
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 81; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, random);
        
        int removed = 0;
        for (int pos : positions) {
            if (removed >= cellsToRemove) break;
            
            int row = pos / 9;
            int col = pos % 9;
            
            // Try removing this cell
            int temp = puzzle[row][col];
            puzzle[row][col] = 0;
            
            // Check if still has unique solution
            if (solver.hasUniqueSolution(puzzle)) {
                removed++;
                
                // Also remove symmetric cell for aesthetics
                int symRow = 8 - row;
                int symCol = 8 - col;
                if (puzzle[symRow][symCol] != 0 && removed < cellsToRemove) {
                    puzzle[symRow][symCol] = 0;
                    removed++;
                }
            } else {
                // Restore cell if removing it creates multiple solutions
                puzzle[row][col] = temp;
            }
        }
        
        return puzzle;
    }
    
    /**
     * Check if placing a number is valid according to Sudoku rules.
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
     * Get shuffled list of numbers 1-9.
     */
    private List<Integer> getShuffledNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);
        return numbers;
    }
    
    /**
     * Convert 2D grid to 81-character string.
     */
    private String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                sb.append(grid[r][c]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Deep copy of grid.
     */
    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 9);
        }
        return copy;
    }
    
    /**
     * Result class containing puzzle and solution.
     */
    public static class Puzzle {
        public final String puzzle;      // Puzzle with clues (81 chars, '0' = empty)
        public final String solution;    // Complete solution (81 chars)
        public final int clueCount;      // Number of given clues
        
        public Puzzle(String puzzle, String solution, int clueCount) {
            this.puzzle = puzzle;
            this.solution = solution;
            this.clueCount = clueCount;
        }
    }
}
