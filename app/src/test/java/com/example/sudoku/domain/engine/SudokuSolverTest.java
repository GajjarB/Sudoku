package com.example.sudoku.domain.engine;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for SudokuSolver: correctness, unique solution detection, grid conversion.
 */
public class SudokuSolverTest {

    private SudokuSolver solver;

    @Before
    public void setUp() {
        solver = new SudokuSolver();
    }

    // ═══════════════════════════════════════════════════════════════
    // SOLVE
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void solve_validPuzzle_returnsTrue() {
        int[][] grid = createEasyPuzzle();
        boolean solved = solver.solve(grid);
        assertTrue("Should solve a valid puzzle", solved);
    }

    @Test
    public void solve_completesGrid_noZerosRemain() {
        int[][] grid = createEasyPuzzle();
        solver.solve(grid);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                assertTrue("Cell (" + r + "," + c + ") should be filled",
                        grid[r][c] >= 1 && grid[r][c] <= 9);
            }
        }
    }

    @Test
    public void solve_producesValidSolution_noConflicts() {
        int[][] grid = createEasyPuzzle();
        solver.solve(grid);
        assertTrue("Solution should have no Sudoku violations", isValidSudoku(grid));
    }

    @Test
    public void solve_emptyGrid_solvable() {
        int[][] grid = new int[9][9];
        assertTrue("Empty grid should be solvable", solver.solve(grid));
        assertTrue("Solution should be valid", isValidSudoku(grid));
    }

    @Test
    public void solve_alreadySolved_returnsTrue() {
        int[][] grid = createSolvedGrid();
        assertTrue("Already solved grid should return true", solver.solve(grid));
    }

    @Test
    public void solve_unsolvablePuzzle_returnsFalse() {
        int[][] grid = new int[9][9];
        // Place conflicting values: two 1s in same row
        grid[0][0] = 1;
        grid[0][1] = 1;
        assertFalse("Conflicting grid should not be solvable", solver.solve(grid));
    }

    // ═══════════════════════════════════════════════════════════════
    // UNIQUE SOLUTION
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void hasUniqueSolution_uniquePuzzle_returnsTrue() {
        int[][] grid = createEasyPuzzle();
        assertTrue("Easy puzzle should have unique solution", solver.hasUniqueSolution(grid));
    }

    @Test
    public void hasUniqueSolution_doesNotMutateOriginalGrid() {
        int[][] grid = createEasyPuzzle();
        int[][] originalCopy = copyGrid(grid);

        solver.hasUniqueSolution(grid);

        for (int r = 0; r < 9; r++) {
            assertArrayEquals("Row " + r + " should not be mutated",
                    originalCopy[r], grid[r]);
        }
    }

    @Test
    public void hasUniqueSolution_emptyGrid_notUnique() {
        int[][] grid = new int[9][9];
        assertFalse("Empty grid has multiple solutions", solver.hasUniqueSolution(grid));
    }

    @Test
    public void hasUniqueSolution_unsolvableGrid_returnsFalse() {
        int[][] grid = new int[9][9];
        grid[0][0] = 1;
        grid[0][1] = 1;
        assertFalse("Unsolvable grid should return false", solver.hasUniqueSolution(grid));
    }

    @Test
    public void hasUniqueSolution_invalidValue_returnsFalseWithoutMutation() {
        int[][] grid = createEasyPuzzle();
        grid[0][0] = 10;
        int[][] originalCopy = copyGrid(grid);

        assertFalse("Invalid values should not be unique", solver.hasUniqueSolution(grid));

        for (int r = 0; r < 9; r++) {
            assertArrayEquals("Row " + r + " should not be mutated",
                    originalCopy[r], grid[r]);
        }
    }

    @Test
    public void isValidPlacement_checksPlacementWithoutMutation() {
        int[][] grid = createEasyPuzzle();
        int[][] originalCopy = copyGrid(grid);

        assertTrue(solver.isValidPlacement(grid, 0, 2, 4));
        assertFalse(solver.isValidPlacement(grid, 0, 2, 5));

        for (int r = 0; r < 9; r++) {
            assertArrayEquals("Row " + r + " should not be mutated",
                    originalCopy[r], grid[r]);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // STRING CONVERSION
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void stringToGrid_validString_correctConversion() {
        String s = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        int[][] grid = SudokuSolver.stringToGrid(s);
        assertEquals(5, grid[0][0]);
        assertEquals(3, grid[0][1]);
        assertEquals(0, grid[0][2]);
        assertEquals(7, grid[8][7]);
    }

    @Test
    public void gridToString_validGrid_correctConversion() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5;
        grid[0][1] = 3;
        grid[8][8] = 9;
        String s = SudokuSolver.gridToString(grid);
        assertEquals(81, s.length());
        assertEquals('5', s.charAt(0));
        assertEquals('3', s.charAt(1));
        assertEquals('9', s.charAt(80));
    }

    @Test
    public void stringToGrid_roundTrip_preservesData() {
        String original = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        int[][] grid = SudokuSolver.stringToGrid(original);
        String result = SudokuSolver.gridToString(grid);
        assertEquals(original, result);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private int[][] createEasyPuzzle() {
        // Known valid puzzle with unique solution
        String s = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        return SudokuSolver.stringToGrid(s);
    }

    private int[][] createSolvedGrid() {
        int[][] grid = createEasyPuzzle();
        new SudokuSolver().solve(grid);
        return grid;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 9);
        }
        return copy;
    }

    private boolean isValidSudoku(int[][] grid) {
        // Check rows
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int val = grid[r][c];
                if (val < 1 || val > 9 || seen[val]) return false;
                seen[val] = true;
            }
        }
        // Check columns
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int val = grid[r][c];
                if (seen[val]) return false;
                seen[val] = true;
            }
        }
        // Check boxes
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                boolean[] seen = new boolean[10];
                for (int r = br * 3; r < br * 3 + 3; r++) {
                    for (int c = bc * 3; c < bc * 3 + 3; c++) {
                        int val = grid[r][c];
                        if (seen[val]) return false;
                        seen[val] = true;
                    }
                }
            }
        }
        return true;
    }
}
