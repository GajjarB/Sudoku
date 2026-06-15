package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class XYZWingDetectorTest {

    @Test
    public void emptyGridProducesNothing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);
        CandidateElimination.StrategyHit hit = new XYZWingDetector().detect(cg);
        assertFalse(hit.found);
    }

    private void setCandidates(CandidateGrid cg, int r, int c, int[] candidates) {
        for (int v = 1; v <= 9; v++) {
            boolean keep = false;
            for (int can : candidates) {
                if (can == v) {
                    keep = true;
                    break;
                }
            }
            if (!keep) {
                cg.eliminate(r, c, v);
            }
        }
    }

    @Test
    public void testXYZWingFound() {
        int[][] grid = new int[9][9];
        CandidateGrid cg = CandidateGrid.from(grid);

        // We want to test XYZ-Wing.
        // Pivot cell has 3 candidates {x, y, z}.
        // Two wing cells are bi-value: {x, z} and {y, z}.
        // Each wing must share a unit with the pivot.
        // Any cell that sees all three (pivot + both wings) cannot contain z.

        // Setup:
        // Pivot: (0, 0) with {1, 2, 3}
        // Wing1: (0, 2) with {1, 3} (shares row 0 with pivot)
        // Wing2: (2, 0) with {2, 3} (shares col 0 with pivot)
        // Target: (2, 2) which sees (0, 0), (0, 2) and (2, 0) because they are all in box 0.
        // Target candidates: {3, 5} -> should eliminate 3.

        // Clear all cells except the ones we're interested in
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if ((r == 0 && c == 0) || (r == 0 && c == 2) || (r == 2 && c == 0) || (r == 2 && c == 2)) {
                    continue;
                }
                for (int v = 1; v <= 9; v++) {
                    cg.eliminate(r, c, v);
                }
            }
        }

        setCandidates(cg, 0, 0, new int[]{1, 2, 3});
        setCandidates(cg, 0, 2, new int[]{1, 3});
        setCandidates(cg, 2, 0, new int[]{2, 3});
        setCandidates(cg, 2, 2, new int[]{3, 5});

        CandidateElimination.StrategyHit hit = new XYZWingDetector().detect(cg);
        assertTrue(hit.found);
        assertEquals("XYZ_WING", hit.strategyName);
        assertEquals(1, hit.eliminations.size());

        CandidateElimination elim = hit.eliminations.get(0);
        assertEquals(2, elim.row);
        assertEquals(2, elim.col);
        assertEquals(3, elim.value);
    }
}
