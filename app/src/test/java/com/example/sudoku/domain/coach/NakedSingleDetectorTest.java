package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.utils.Constants;
import org.junit.Test;
import static org.junit.Assert.*;

public class NakedSingleDetectorTest {

    @Test
    public void detect_findsNakedSingle() {
        NakedSingleDetector detector = new NakedSingleDetector();
        // A grid where row 0 col 0 has only one candidate (1).
        // Let's create a grid where cell (0, 0) sees 2-9.
        int[][] grid = new int[9][9];
        // We will leave (0,0) empty, and fill the first row with 2-9
        grid[0][1] = 2;
        grid[0][2] = 3;
        grid[0][3] = 4;
        grid[0][4] = 5;
        grid[0][5] = 6;
        grid[0][6] = 7;
        grid[0][7] = 8;
        grid[0][8] = 9;

        // This leaves only 1 as a candidate for (0,0) since 2-9 are in the same row.
        StrategyResult result = detector.detect(grid, null);

        assertTrue(result.found);
        assertEquals(Constants.STRATEGY_NAKED_SINGLE, result.strategyName);
        assertEquals(0, result.targetRow);
        assertEquals(0, result.targetCol);
        assertEquals(1, result.value);
        assertTrue(result.placement);
        assertNotNull(result.highlightCells);
        assertEquals(1, result.highlightCells.size());
        assertEquals(0, result.highlightCells.get(0).row);
        assertEquals(0, result.highlightCells.get(0).col);
        assertNotNull(result.explanationShort);
        assertNotNull(result.explanationLong);
    }

    @Test
    public void detect_noNakedSingle_returnsNone() {
        NakedSingleDetector detector = new NakedSingleDetector();
        // An empty grid has multiple candidates for every cell (1-9).
        int[][] grid = new int[9][9];

        StrategyResult result = detector.detect(grid, null);

        assertFalse(result.found);
    }

    @Test
    public void detect_completedGrid_returnsNone() {
        NakedSingleDetector detector = new NakedSingleDetector();
        // A completed grid string representation (from HintEngineTest)
        int[][] grid = GameValidator.stringToGrid(
                "123456789456789123789123456214365897365897214897214365531642978642978531978531642");

        StrategyResult result = detector.detect(grid, null);

        assertFalse(result.found);
    }
}
