package com.example.sudoku.domain.engine;

/**
 * Solves Sudoku puzzles using backtracking.
 * Used for:
 * - Verifying puzzles have unique solutions
 * - Providing hints to players
 * - Checking if completed puzzle is correct
 */
public class SudokuSolver {
    private static final int GRID_SIZE = SudokuConstants.GRID_SIZE;
    private static final int BOX_SIZE = SudokuConstants.BOX_SIZE;
    private static final int ALL_CANDIDATES = SudokuConstants.ALL_CANDIDATES;

    private final DancingLinksSolver dlx = new DancingLinksSolver();

    /**
     * Solve a Sudoku puzzle (modifies grid in place).
     *
     * @param grid 9x9 grid (0 = empty)
     * @return true if solution found, false if unsolvable
     */
    public boolean solve(int[][] grid) {
        int[] rowMasks = new int[GRID_SIZE];
        int[] colMasks = new int[GRID_SIZE];
        int[] boxMasks = new int[GRID_SIZE];
        if (!initializeMasks(grid, rowMasks, colMasks, boxMasks)) {
            return false;
        }

        return solveBacktrack(grid, rowMasks, colMasks, boxMasks);
    }

    /**
     * Check if puzzle has exactly one unique solution.
     * Critical for puzzle generation!
     */
    public boolean hasUniqueSolution(int[][] grid) {
        if (!isValidShape(grid)) {
            return false;
        }

        int[][] copy = copyGrid(grid);
        int[] rowMasks = new int[GRID_SIZE];
        int[] colMasks = new int[GRID_SIZE];
        int[] boxMasks = new int[GRID_SIZE];
        if (!initializeMasks(copy, rowMasks, colMasks, boxMasks)) {
            return false;
        }

        // DLX is significantly faster than backtracking for uniqueness on hard puzzles.
        return dlx.hasUniqueSolution(copy);
    }

    /**
     * Public placement validator for UI hints and candidate previews.
     */
    public boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        if (!isValidShape(grid) || row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE
                || num < 1 || num > GRID_SIZE) {
            return false;
        }
        if (grid[row][col] != 0 && grid[row][col] != num) {
            return false;
        }

        for (int c = 0; c < GRID_SIZE; c++) {
            if (c != col && grid[row][c] == num) return false;
        }

        for (int r = 0; r < GRID_SIZE; r++) {
            if (r != row && grid[r][col] == num) return false;
        }

        int boxRow = (row / BOX_SIZE) * BOX_SIZE;
        int boxCol = (col / BOX_SIZE) * BOX_SIZE;
        for (int r = boxRow; r < boxRow + BOX_SIZE; r++) {
            for (int c = boxCol; c < boxCol + BOX_SIZE; c++) {
                if ((r != row || c != col) && grid[r][c] == num) return false;
            }
        }

        return true;
    }

    /**
     * Count number of solutions (stops at maxCount for efficiency).
     */
    private int countSolutions(
            int[][] grid,
            int[] rowMasks,
            int[] colMasks,
            int[] boxMasks,
            int maxCount
    ) {
        int[] cell = findMostConstrainedEmptyCell(grid, rowMasks, colMasks, boxMasks);
        if (cell == null) {
            return 1;
        }

        int row = cell[0];
        int col = cell[1];
        int candidates = cell[2];
        if (candidates == 0) {
            return 0;
        }

        int count = 0;
        while (candidates != 0) {
            int bit = candidates & -candidates;
            int num = Integer.numberOfTrailingZeros(bit);
            place(grid, rowMasks, colMasks, boxMasks, row, col, num);
            count += countSolutions(grid, rowMasks, colMasks, boxMasks, maxCount);
            remove(grid, rowMasks, colMasks, boxMasks, row, col, num);
            if (count >= maxCount) {
                return count;
            }
            candidates &= ~bit;
        }

        return count;
    }

    /**
     * Backtracking solver using MRV to choose the next cell.
     */
    private boolean solveBacktrack(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks) {
        int[] cell = findMostConstrainedEmptyCell(grid, rowMasks, colMasks, boxMasks);
        if (cell == null) {
            return true;
        }

        int row = cell[0];
        int col = cell[1];
        int candidates = cell[2];
        if (candidates == 0) {
            return false;
        }

        while (candidates != 0) {
            int bit = candidates & -candidates;
            int num = Integer.numberOfTrailingZeros(bit);
            place(grid, rowMasks, colMasks, boxMasks, row, col, num);
            if (solveBacktrack(grid, rowMasks, colMasks, boxMasks)) {
                return true;
            }
            remove(grid, rowMasks, colMasks, boxMasks, row, col, num);
            candidates &= ~bit;
        }

        return false;
    }

    /**
     * Check if number placement is valid.
     */
    private boolean isValid(int[][] grid, int row, int col, int num) {
        for (int c = 0; c < GRID_SIZE; c++) {
            if (grid[row][c] == num) return false;
        }

        for (int r = 0; r < GRID_SIZE; r++) {
            if (grid[r][col] == num) return false;
        }

        int boxRow = (row / BOX_SIZE) * BOX_SIZE;
        int boxCol = (col / BOX_SIZE) * BOX_SIZE;
        for (int r = boxRow; r < boxRow + BOX_SIZE; r++) {
            for (int c = boxCol; c < boxCol + BOX_SIZE; c++) {
                if (grid[r][c] == num) return false;
            }
        }

        return true;
    }

    private boolean initializeMasks(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks) {
        if (!isValidShape(grid)) {
            return false;
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = grid[row][col];
                if (value == 0) continue;
                if (value < 1 || value > GRID_SIZE) return false;

                int bit = bitFor(value);
                int box = boxIndex(row, col);
                if ((rowMasks[row] & bit) != 0 || (colMasks[col] & bit) != 0
                        || (boxMasks[box] & bit) != 0) {
                    return false;
                }

                rowMasks[row] |= bit;
                colMasks[col] |= bit;
                boxMasks[box] |= bit;
            }
        }
        return true;
    }

    private int[] findMostConstrainedEmptyCell(
            int[][] grid,
            int[] rowMasks,
            int[] colMasks,
            int[] boxMasks
    ) {
        int bestRow = -1;
        int bestCol = -1;
        int bestCandidates = 0;
        int bestCount = Integer.MAX_VALUE;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] != 0) continue;

                int used = rowMasks[row] | colMasks[col] | boxMasks[boxIndex(row, col)];
                int candidates = ALL_CANDIDATES & ~used;
                int candidateCount = Integer.bitCount(candidates);
                if (candidateCount < bestCount) {
                    bestRow = row;
                    bestCol = col;
                    bestCandidates = candidates;
                    bestCount = candidateCount;
                    if (candidateCount <= 1) {
                        return new int[]{bestRow, bestCol, bestCandidates};
                    }
                }
            }
        }

        return bestRow == -1 ? null : new int[]{bestRow, bestCol, bestCandidates};
    }

    private void place(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks, int row, int col, int num) {
        int bit = bitFor(num);
        grid[row][col] = num;
        rowMasks[row] |= bit;
        colMasks[col] |= bit;
        boxMasks[boxIndex(row, col)] |= bit;
    }

    private void remove(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks, int row, int col, int num) {
        int bit = bitFor(num);
        grid[row][col] = 0;
        rowMasks[row] &= ~bit;
        colMasks[col] &= ~bit;
        boxMasks[boxIndex(row, col)] &= ~bit;
    }

    private int bitFor(int num) {
        return 1 << num;
    }

    private int boxIndex(int row, int col) {
        return (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE);
    }

    private boolean isValidShape(int[][] grid) {
        if (grid == null || grid.length != GRID_SIZE) {
            return false;
        }
        for (int row = 0; row < GRID_SIZE; row++) {
            if (grid[row] == null || grid[row].length != GRID_SIZE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deep copy grid.
     */
    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int r = 0; r < GRID_SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, GRID_SIZE);
        }
        return copy;
    }

    /**
     * Convert string to 2D array.
     */
    public static int[][] stringToGrid(String s) {
        return GameValidator.stringToGrid(s);
    }

    /**
     * Convert 2D array to string.
     */
    public static String gridToString(int[][] grid) {
        return GameValidator.gridToString(grid);
    }
}
