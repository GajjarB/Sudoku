package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class ForcingChainDetectorTest {

    @Test
    public void emptyGridProducesNothing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);
        CandidateElimination.StrategyHit hit = new ForcingChainDetector().detect(cg);
        assertFalse(hit.found);
    }

    @Test
    public void detectsForcingChainWithContradiction() {
        int[][] grid = new int[9][9];
        // Target: (1,1) we want to eliminate 1.
        // Pivot: (0,0) will have candidates {1, 2}.
        grid[0][2] = 3; grid[0][3] = 4; grid[0][4] = 5;
        grid[0][5] = 6; grid[0][6] = 7; grid[0][7] = 8; grid[0][8] = 9;

        grid[4][0] = 8; grid[5][0] = 9;
        grid[1][3] = 2; grid[1][4] = 3; grid[1][5] = 4;
        grid[1][6] = 5; grid[1][7] = 6; grid[1][8] = 7;

        CandidateGrid cg = CandidateGrid.from(grid);
        CandidateElimination.StrategyHit hit = new ForcingChainDetector().detect(cg);

        assertTrue("Expected to find a forcing chain", hit.found);
        assertEquals("FORCING_CHAIN", hit.strategyName);

        boolean foundElimination = false;
        for (CandidateElimination elim : hit.eliminations) {
            if (elim.row == 1 && elim.col == 1 && elim.value == 1) {
                foundElimination = true;
                break;
            }
        }
        assertTrue("Expected to eliminate 1 from (1,1)", foundElimination);
    }
}
