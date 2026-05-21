package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class PointingPairDetectorTest {

    @Test
    public void findsPointingPair() {
        int[][] grid = new int[9][9];
        // Place 3 inside the top-left box at columns 1 and 2 of rows 1,2
        // so the only remaining candidates for 3 in box 0 are row 0.
        // Boxes 1 and 2 don't contain 3 yet, so row 0 elims are real.
        grid[1][2] = 3;
        grid[2][1] = 3;
        CandidateGrid cg = CandidateGrid.from(grid);

        CandidateElimination.StrategyHit hit = new PointingPairDetector().detect(cg);
        assertTrue(hit.found);
        // Ensure at least one elimination targets row 0 outside the top-left box.
        boolean rowZeroElim = hit.eliminations.stream()
                .anyMatch(e -> e.row == 0 && e.col >= 3 && e.value == 3);
        assertTrue(rowZeroElim);
    }
}
