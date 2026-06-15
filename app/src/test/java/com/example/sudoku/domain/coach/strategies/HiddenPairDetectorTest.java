package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiddenPairDetectorTest {

    @Test
    public void emptyGridProducesNothing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);
        CandidateElimination.StrategyHit hit = new HiddenPairDetector().detect(cg);
        assertFalse(hit.found);
    }

    @Test
    public void detectsHiddenPairInRow() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        // Make candidates 1 and 2 only possible in cols 0 and 1 for row 0.
        for (int c = 2; c < 9; c++) {
            cg.eliminate(0, c, 1);
            cg.eliminate(0, c, 2);
        }

        CandidateElimination.StrategyHit hit = new HiddenPairDetector().detect(cg);
        assertTrue(hit.found);
        assertEquals("HIDDEN_PAIR", hit.strategyName);

        // Eliminations should be 3..9 in (0,0) and (0,1).
        // 7 candidates * 2 cells = 14 eliminations.
        assertEquals(14, hit.eliminations.size());

        // Verify highlights
        assertEquals(2, hit.highlightCells.size());
        assertTrue(hit.highlightCells.stream().anyMatch(p -> p.row == 0 && p.col == 0));
        assertTrue(hit.highlightCells.stream().anyMatch(p -> p.row == 0 && p.col == 1));
    }

    @Test
    public void detectsHiddenPairInCol() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        // Make candidates 3 and 4 only possible in rows 3 and 4 for col 2.
        for (int r = 0; r < 9; r++) {
            if (r != 3 && r != 4) {
                cg.eliminate(r, 2, 3);
                cg.eliminate(r, 2, 4);
            }
        }

        CandidateElimination.StrategyHit hit = new HiddenPairDetector().detect(cg);
        assertTrue(hit.found);
        assertEquals("HIDDEN_PAIR", hit.strategyName);

        assertEquals(14, hit.eliminations.size());
    }

    @Test
    public void detectsHiddenPairInBox() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        // Box 0 is rows 0..2, cols 0..2.
        // Make candidates 8 and 9 only possible in (1,1) and (2,2).
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if ((r == 1 && c == 1) || (r == 2 && c == 2)) {
                    continue;
                }
                cg.eliminate(r, c, 8);
                cg.eliminate(r, c, 9);
            }
        }

        CandidateElimination.StrategyHit hit = new HiddenPairDetector().detect(cg);
        assertTrue(hit.found);
        assertEquals("HIDDEN_PAIR", hit.strategyName);

        assertEquals(14, hit.eliminations.size());
    }
}
