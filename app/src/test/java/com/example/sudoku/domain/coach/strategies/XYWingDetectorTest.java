package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class XYWingDetectorTest {

    @Test
    public void emptyGridProducesNothing() {
        CandidateGrid cg = CandidateGrid.from(new int[9][9]);
        CandidateElimination.StrategyHit hit = new XYWingDetector().detect(cg);
        assertFalse(hit.found);
    }
}
