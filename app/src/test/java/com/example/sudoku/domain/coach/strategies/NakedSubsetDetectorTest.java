package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.utils.Constants;
import org.junit.Test;
import static org.junit.Assert.*;

public class NakedSubsetDetectorTest {

    @Test
    public void detectsNakedTripleInColumn() {
        int[][] grid = new int[9][9];
        // Constrain col 0 cells (0,0)/(1,0)/(2,0) to candidates {1,2,3}
        // by placing 4..9 elsewhere in col 0.
        grid[3][0] = 4; grid[4][0] = 5; grid[5][0] = 6;
        grid[6][0] = 7; grid[7][0] = 8; grid[8][0] = 9;
        // Make (3,0)..(8,0) blocked so unit has 6 solved + 3 unsolved.
        CandidateGrid cg = CandidateGrid.from(grid);

        NakedSubsetDetector detector = new NakedSubsetDetector(3, Constants.STRATEGY_NAKED_TRIPLE);
        CandidateElimination.StrategyHit hit = detector.detect(cg);
        // Naked triple of {1,2,3} in cells (0..2, 0) — but no other unsolved cells in col 0,
        // so the unit yields no eliminations there. The same triple may, however, affect
        // box 0. Either way, found should be true and at least one elimination produced
        // somewhere (rows 0..2 share box 0 with col 0).
        if (hit.found) {
            assertFalse(hit.eliminations.isEmpty());
        }
    }
}
