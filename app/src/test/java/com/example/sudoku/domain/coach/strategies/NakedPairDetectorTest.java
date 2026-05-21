package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateGrid;
import org.junit.Test;
import static org.junit.Assert.*;

public class NakedPairDetectorTest {

    @Test
    public void detectsNakedPairInRow() {
        int[][] grid = new int[9][9];
        // Force cells (0,0) and (0,1) to candidates {1,2}; eliminate from rest of row.
        // Place 3..9 in the same row so (0,0) and (0,1) only allow 1 and 2.
        grid[0][2] = 3; grid[0][3] = 4; grid[0][4] = 5;
        grid[0][5] = 6; grid[0][6] = 7; grid[0][7] = 8; grid[0][8] = 9;
        CandidateGrid cg = CandidateGrid.from(grid);
        // (0,0) and (0,1) candidates == {1,2}. But the row has no other empty cell.
        // To create elimination targets we instead carve a column scenario.

        int[][] colGrid = new int[9][9];
        // Make cells (0,0) and (1,0) have candidates {1,2} only.
        colGrid[2][0] = 3; colGrid[3][0] = 4; colGrid[4][0] = 5;
        colGrid[5][0] = 6; colGrid[6][0] = 7; colGrid[7][0] = 8; colGrid[8][0] = 9;
        // Cells (0,1)..(8,1) free for eliminations.
        CandidateGrid cgCol = CandidateGrid.from(colGrid);

        CandidateElimination.StrategyHit hit = new NakedPairDetector().detect(cgCol);
        assertTrue(hit.found);
        assertFalse(hit.eliminations.isEmpty());
    }
}
