package com.example.sudoku.domain.coach;

import org.junit.Test;
import static org.junit.Assert.*;

public class CandidateGridTest {

    @Test
    public void from_emptyCell_hasAllCandidates() {
        int[][] grid = new int[9][9];
        CandidateGrid cg = CandidateGrid.from(grid);
        assertEquals(CandidateGrid.ALL, cg.candidates(0, 0));
        assertEquals(9, Integer.bitCount(cg.candidates(0, 0)));
    }

    @Test
    public void from_clueCell_zeroCandidates() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5;
        CandidateGrid cg = CandidateGrid.from(grid);
        assertEquals(0, cg.candidates(0, 0));
    }

    @Test
    public void from_clueExcludesPeers() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5;
        CandidateGrid cg = CandidateGrid.from(grid);
        assertEquals(0, cg.candidates(0, 4) & (1 << 5));
        assertEquals(0, cg.candidates(4, 0) & (1 << 5));
        assertEquals(0, cg.candidates(2, 2) & (1 << 5));
        // Cell outside row/col/box keeps 5.
        assertNotEquals(0, cg.candidates(4, 4) & (1 << 5));
    }

    @Test
    public void place_prunesPeers() {
        int[][] grid = new int[9][9];
        CandidateGrid cg = CandidateGrid.from(grid);
        assertTrue(cg.place(0, 0, 3));
        assertEquals(0, cg.candidates(0, 5) & (1 << 3));
        assertEquals(0, cg.candidates(5, 0) & (1 << 3));
    }

    @Test
    public void eliminate_removesBit() {
        int[][] grid = new int[9][9];
        CandidateGrid cg = CandidateGrid.from(grid);
        assertTrue(cg.eliminate(0, 0, 7));
        assertEquals(0, cg.candidates(0, 0) & (1 << 7));
        assertFalse(cg.eliminate(0, 0, 7));
    }
}
