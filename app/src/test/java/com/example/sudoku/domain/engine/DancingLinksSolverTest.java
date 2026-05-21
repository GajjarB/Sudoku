package com.example.sudoku.domain.engine;

import org.junit.Test;
import static org.junit.Assert.*;

public class DancingLinksSolverTest {

    private static final String SOLVABLE_PUZZLE =
            "530070000" +
            "600195000" +
            "098000060" +
            "800060003" +
            "400803001" +
            "700020006" +
            "060000280" +
            "000419005" +
            "000080079";

    private static final String SOLUTION =
            "534678912" +
            "672195348" +
            "198342567" +
            "859761423" +
            "426853791" +
            "713924856" +
            "961537284" +
            "287419635" +
            "345286179";

    @Test
    public void solve_returnsCorrectSolution() {
        int[][] grid = SudokuSolver.stringToGrid(SOLVABLE_PUZZLE);
        int[][] solved = new DancingLinksSolver().solve(grid);

        assertNotNull(solved);
        assertEquals(SOLUTION, SudokuSolver.gridToString(solved));
    }

    @Test
    public void hasUniqueSolution_uniquePuzzle_returnsTrue() {
        int[][] grid = SudokuSolver.stringToGrid(SOLVABLE_PUZZLE);
        assertTrue(new DancingLinksSolver().hasUniqueSolution(grid));
    }

    @Test
    public void hasUniqueSolution_emptyGrid_returnsFalse() {
        int[][] grid = new int[9][9];
        assertFalse(new DancingLinksSolver().hasUniqueSolution(grid));
    }

    @Test
    public void countSolutions_stopsAtMaxCount() {
        int[][] grid = new int[9][9];
        int count = new DancingLinksSolver().countSolutions(grid, 5);
        assertEquals(5, count);
    }
}
