package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.SudokuSolver;
import org.junit.Test;
import static org.junit.Assert.*;

public class LogicalSolverTest {

    private static final String EASY_PUZZLE =
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
    public void solvesEasyPuzzleWithSinglesOnly() {
        int[][] puzzle = SudokuSolver.stringToGrid(EASY_PUZZLE);
        int[][] solution = SudokuSolver.stringToGrid(SOLUTION);
        LogicalSolver.Result result = new LogicalSolver().solve(puzzle, solution);

        assertTrue(result.solved);
        assertEquals(SOLUTION, SudokuSolver.gridToString(result.grid));
        assertTrue(result.strategyUsage.containsKey("NAKED_SINGLE")
                || result.strategyUsage.containsKey("HIDDEN_SINGLE"));
    }

    @Test
    public void reportsStrategiesUsed() {
        int[][] puzzle = SudokuSolver.stringToGrid(EASY_PUZZLE);
        int[][] solution = SudokuSolver.stringToGrid(SOLUTION);
        LogicalSolver.Result result = new LogicalSolver().solve(puzzle, solution);
        assertFalse(result.strategyUsage.isEmpty());
    }
}
