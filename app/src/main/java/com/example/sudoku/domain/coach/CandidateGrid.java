package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.SudokuConstants;

/**
 * Per-cell pencil-mark candidates stored as bitmasks (bits 1..9).
 *
 * Filled cells have candidates == 0. Higher-order strategies prune bits;
 * placement reapplies row/col/box exclusion to peers.
 */
public final class CandidateGrid {

    public static final int N = SudokuConstants.GRID_SIZE;
    public static final int B = SudokuConstants.BOX_SIZE;
    public static final int ALL = SudokuConstants.ALL_CANDIDATES;

    private final int[][] candidates = new int[N][N];
    private final int[][] grid = new int[N][N];

    public static CandidateGrid from(int[][] sourceGrid) {
        CandidateGrid cg = new CandidateGrid();
        int[] rowMask = new int[N];
        int[] colMask = new int[N];
        int[] boxMask = new int[N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int v = sourceGrid[r][c];
                cg.grid[r][c] = v;
                if (v != 0) {
                    int bit = 1 << v;
                    rowMask[r] |= bit;
                    colMask[c] |= bit;
                    boxMask[boxIndex(r, c)] |= bit;
                }
            }
        }
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (cg.grid[r][c] == 0) {
                    int used = rowMask[r] | colMask[c] | boxMask[boxIndex(r, c)];
                    cg.candidates[r][c] = ALL & ~used;
                }
            }
        }
        return cg;
    }

    public int value(int row, int col) { return grid[row][col]; }

    public int candidates(int row, int col) { return candidates[row][col]; }

    public boolean isSolved(int row, int col) { return grid[row][col] != 0; }

    public int[][] gridCopy() {
        int[][] copy = new int[N][N];
        for (int r = 0; r < N; r++) copy[r] = grid[r].clone();
        return copy;
    }

    /** Place value at (row, col), pruning peers. Returns false if already filled or invalid. */
    public boolean place(int row, int col, int value) {
        if (grid[row][col] != 0) return false;
        int bit = 1 << value;
        if ((candidates[row][col] & bit) == 0) return false;
        grid[row][col] = value;
        candidates[row][col] = 0;
        int boxR = (row / B) * B;
        int boxC = (col / B) * B;
        for (int i = 0; i < N; i++) {
            candidates[row][i] &= ~bit;
            candidates[i][col] &= ~bit;
        }
        for (int r = boxR; r < boxR + B; r++) {
            for (int c = boxC; c < boxC + B; c++) {
                candidates[r][c] &= ~bit;
            }
        }
        return true;
    }

    /** Remove a candidate bit. Returns true if the bit was previously set. */
    public boolean eliminate(int row, int col, int value) {
        int bit = 1 << value;
        if (grid[row][col] != 0) return false;
        if ((candidates[row][col] & bit) == 0) return false;
        candidates[row][col] &= ~bit;
        return true;
    }

    public boolean isComplete() {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (grid[r][c] == 0) return false;
            }
        }
        return true;
    }

    public static int boxIndex(int row, int col) {
        return (row / B) * B + (col / B);
    }
}
