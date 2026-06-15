package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class XWingDetectorTest {

    @Test
    public void detectsRowBasedXWing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        int digit = 1;
        int r1 = 1;
        int r2 = 4;
        int c1 = 2;
        int c2 = 7;

        // Setup row-based X-Wing:
        // Candidate 'digit' appears exactly in columns c1 and c2 for rows r1 and r2.
        for (int c = 0; c < 9; c++) {
            if (c != c1 && c != c2) {
                cg.eliminate(r1, c, digit);
                cg.eliminate(r2, c, digit);
            }
        }

        XWingDetector detector = new XWingDetector();
        CandidateElimination.StrategyHit hit = detector.detect(cg);

        assertTrue(hit.found);
        assertEquals("X-Wing on digit 1 between rows 2 and 5.", hit.explanation);
        assertEquals(14, hit.eliminations.size()); // 9 rows - 2 X-wing rows = 7 rows * 2 cols = 14 eliminations

        for (CandidateElimination elim : hit.eliminations) {
            assertEquals(digit, elim.value);
            assertTrue(elim.col == c1 || elim.col == c2);
            assertNotEquals(r1, elim.row);
            assertNotEquals(r2, elim.row);
        }

        assertEquals(4, hit.highlightCells.size());
    }

    @Test
    public void detectsColBasedXWing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        int digit = 5;
        int c1 = 3;
        int c2 = 8;
        int r1 = 0;
        int r2 = 5;

        // Setup col-based X-Wing:
        // Candidate 'digit' appears exactly in rows r1 and r2 for columns c1 and c2.
        for (int r = 0; r < 9; r++) {
            if (r != r1 && r != r2) {
                cg.eliminate(r, c1, digit);
                cg.eliminate(r, c2, digit);
            }
        }

        XWingDetector detector = new XWingDetector();
        CandidateElimination.StrategyHit hit = detector.detect(cg);

        assertTrue(hit.found);
        assertEquals("X-Wing on digit 5 between columns 4 and 9.", hit.explanation);
        assertEquals(14, hit.eliminations.size()); // 9 cols - 2 X-wing cols = 7 cols * 2 rows = 14 eliminations

        for (CandidateElimination elim : hit.eliminations) {
            assertEquals(digit, elim.value);
            assertTrue(elim.row == r1 || elim.row == r2);
            assertNotEquals(c1, elim.col);
            assertNotEquals(c2, elim.col);
        }

        assertEquals(4, hit.highlightCells.size());
    }

    @Test
    public void returnsNoneWhenNoXWing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);

        // Standard full grid has no X-wings because candidates appear in all columns/rows,
        // so rowCols/colRows count is 9, not 2.

        XWingDetector detector = new XWingDetector();
        CandidateElimination.StrategyHit hit = detector.detect(cg);

        assertFalse(hit.found);
        assertTrue(hit.eliminations.isEmpty());
    }
}
