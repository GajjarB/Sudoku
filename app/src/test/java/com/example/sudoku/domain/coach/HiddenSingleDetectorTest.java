package com.example.sudoku.domain.coach;

import com.example.sudoku.utils.Constants;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiddenSingleDetectorTest {

    @Test
    public void testHiddenSingleInRow() {
        int[][] grid = new int[9][9];
        int[][] solution = new int[9][9];

        // We want 1 to be the only option in cell (0, 0) for ROW 0.
        // We must ensure that (0,0) is NOT a hidden single for Column 0 or Box 0.
        // To do this, we don't even need to be fancy if we just accept it might be a column single.
        // BUT the assertion looks for "row".
        // HiddenSingleDetector scans rows FIRST. So if it's the only one in the row, it will return "row".
        // It doesn't matter if it's also the only one in the col or box.

        grid[3][1] = 1;
        grid[4][2] = 1;
        grid[1][3] = 1;
        grid[2][4] = 1;
        grid[6][5] = 1;
        grid[7][6] = 1;
        grid[8][7] = 1;
        grid[5][8] = 1;

        HiddenSingleDetector detector = new HiddenSingleDetector();
        StrategyResult result = detector.detect(grid, solution);

        assertTrue("Strategy should find a hidden single", result.found);
        assertEquals(Constants.STRATEGY_HIDDEN_SINGLE, result.strategyName);
        assertEquals(0, result.targetRow);
        assertEquals(0, result.targetCol);
        assertEquals(1, result.value);
        assertTrue(result.explanationShort.contains("row"));
    }

    @Test
    public void testHiddenSingleInColumn() {
        int[][] grid = new int[9][9];
        int[][] solution = new int[9][9];

        // We want 2 to be the only option in cell (0, 0) for COLUMN 0.
        // The detector scans rows first. We need to ensure that row 0 has another open spot for 2.
        // We need to block cells (1,0) through (8,0).
        grid[1][3] = 2;
        grid[2][4] = 2;
        grid[3][1] = 2;
        grid[6][1] = 2;

        HiddenSingleDetector detector = new HiddenSingleDetector();
        StrategyResult result = detector.detect(grid, solution);

        assertTrue("Strategy should find a hidden single", result.found);
        assertEquals(Constants.STRATEGY_HIDDEN_SINGLE, result.strategyName);
        assertEquals(0, result.targetRow);
        assertEquals(0, result.targetCol);
        assertEquals(2, result.value);
        assertTrue(result.explanationShort.contains("column"));
    }

    @Test
    public void testHiddenSingleInBox() {
        int[][] grid = new int[9][9];
        int[][] solution = new int[9][9];

        grid[3][1] = 3;
        grid[4][2] = 3;
        grid[1][3] = 3;
        grid[2][4] = 3;

        HiddenSingleDetector detector = new HiddenSingleDetector();
        StrategyResult result = detector.detect(grid, solution);

        assertTrue("Strategy should find a hidden single", result.found);
        assertEquals(Constants.STRATEGY_HIDDEN_SINGLE, result.strategyName);
        assertEquals(0, result.targetRow);
        assertEquals(0, result.targetCol);
        assertEquals(3, result.value);
        assertTrue(result.explanationShort.contains("box"));
    }

    @Test
    public void testNoHiddenSingle() {
        int[][] grid = new int[9][9];
        int[][] solution = new int[9][9];
        HiddenSingleDetector detector = new HiddenSingleDetector();
        StrategyResult result = detector.detect(grid, solution);

        assertFalse("Strategy should not find anything", result.found);
    }
}
