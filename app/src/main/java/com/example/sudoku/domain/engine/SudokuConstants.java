package com.example.sudoku.domain.engine;

/**
 * Shared constants for the 9x9 classic Sudoku engine.
 * Variant boards (6x6, 12x12) will define their own constants.
 */
public final class SudokuConstants {

    private SudokuConstants() {}

    public static final int GRID_SIZE = 9;
    public static final int BOX_SIZE = 3;
    public static final int TOTAL_CELLS = GRID_SIZE * GRID_SIZE;

    /** Minimum number of clues that can yield a uniquely solvable Sudoku (proven 2012). */
    public static final int MIN_UNIQUE_CLUES = 17;
    public static final int MAX_CLUES = TOTAL_CELLS;

    /** Bitmask covering candidates 1..9 (bits 1..9 set). */
    public static final int ALL_CANDIDATES = 0x3FE;

    public static int bitFor(int value) {
        return 1 << value;
    }

    public static int boxIndex(int row, int col) {
        return (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE);
    }
}
