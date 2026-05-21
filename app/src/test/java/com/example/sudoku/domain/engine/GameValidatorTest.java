package com.example.sudoku.domain.engine;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests for GameValidator: move validation, conflict detection, candidates, completion.
 */
public class GameValidatorTest {

    // ═══════════════════════════════════════════════════════════════
    // isValidMove
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void isValidMove_emptyCell_validNumber_returnsTrue() {
        int[][] grid = createTestGrid();
        // Cell (0,2) is 0, and 1 not in row/col/box
        assertTrue(GameValidator.isValidMove(grid, 0, 2, 1));
    }

    @Test
    public void isValidMove_filledCell_returnsFalse() {
        int[][] grid = createTestGrid();
        // Cell (0,0) is 5 (filled)
        assertFalse(GameValidator.isValidMove(grid, 0, 0, 3));
    }

    @Test
    public void isValidMove_conflictInRow_returnsFalse() {
        int[][] grid = createTestGrid();
        // Row 0 has 5 at (0,0), placing 5 at (0,2) should fail
        assertFalse(GameValidator.isValidMove(grid, 0, 2, 5));
    }

    @Test
    public void isValidMove_conflictInColumn_returnsFalse() {
        int[][] grid = createTestGrid();
        // Col 0 has 5 at (0,0), placing 5 at empty cell in same col should fail
        // Col 0: 5,6,0,8,4,7,0,0,0 — placing 6 at (2,0) conflicts with (1,0)
        assertFalse(GameValidator.isValidMove(grid, 2, 0, 6));
    }

    @Test
    public void isValidMove_conflictInBox_returnsFalse() {
        int[][] grid = createTestGrid();
        // Box (0,0) contains 5,3,6,0,9,8,0,0,0 — placing 9 at (2,0) conflicts
        assertFalse(GameValidator.isValidMove(grid, 2, 0, 9));
    }

    @Test
    public void isValidMove_noConflict_returnsTrue() {
        int[][] grid = new int[9][9];
        grid[0][0] = 1;
        assertTrue(GameValidator.isValidMove(grid, 0, 1, 2));
    }

    // ═══════════════════════════════════════════════════════════════
    // getConflictingCells
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void getConflictingCells_noConflicts_returnsEmptyList() {
        int[][] grid = new int[9][9];
        grid[0][0] = 1;
        List<int[]> conflicts = GameValidator.getConflictingCells(grid, 1, 1, 2);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    public void getConflictingCells_rowConflict_returnsConflictingCell() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5;
        List<int[]> conflicts = GameValidator.getConflictingCells(grid, 0, 5, 5);
        assertFalse("Should find row conflict", conflicts.isEmpty());
        assertEquals(0, conflicts.get(0)[0]); // Same row
        assertEquals(0, conflicts.get(0)[1]); // Col 0
    }

    @Test
    public void getConflictingCells_multipleConflicts_returnsAll() {
        int[][] grid = new int[9][9];
        grid[0][0] = 5; // Row conflict
        grid[5][5] = 5; // Column conflict
        List<int[]> conflicts = GameValidator.getConflictingCells(grid, 0, 5, 5);
        assertTrue("Should find multiple conflicts", conflicts.size() >= 2);
    }

    // ═══════════════════════════════════════════════════════════════
    // isCompleteSolution
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void isCompleteSolution_matchingSolution_returnsTrue() {
        int[][] grid = createSolvedGrid();
        int[][] solution = copyGrid(grid);
        assertTrue(GameValidator.isCompleteSolution(grid, solution));
    }

    @Test
    public void isCompleteSolution_differentValues_returnsFalse() {
        int[][] grid = createSolvedGrid();
        int[][] solution = copyGrid(grid);
        grid[0][0] = grid[0][0] == 1 ? 2 : 1; // Change one value
        assertFalse(GameValidator.isCompleteSolution(grid, solution));
    }

    @Test
    public void isCompleteSolution_incompleteGrid_returnsFalse() {
        int[][] grid = createSolvedGrid();
        int[][] solution = copyGrid(grid);
        grid[4][4] = 0; // Make one cell empty
        assertFalse(GameValidator.isCompleteSolution(grid, solution));
    }

    // ═══════════════════════════════════════════════════════════════
    // isGridComplete
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void isGridComplete_fullGrid_returnsTrue() {
        int[][] grid = createSolvedGrid();
        assertTrue(GameValidator.isGridComplete(grid));
    }

    @Test
    public void isGridComplete_emptyCell_returnsFalse() {
        int[][] grid = createSolvedGrid();
        grid[4][4] = 0;
        assertFalse(GameValidator.isGridComplete(grid));
    }

    @Test
    public void isGridComplete_emptyGrid_returnsFalse() {
        int[][] grid = new int[9][9];
        assertFalse(GameValidator.isGridComplete(grid));
    }

    // ═══════════════════════════════════════════════════════════════
    // getCandidates
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void getCandidates_filledCell_returnsEmptyList() {
        int[][] grid = createTestGrid();
        List<Integer> candidates = GameValidator.getCandidates(grid, 0, 0);
        assertTrue("Filled cell should have no candidates", candidates.isEmpty());
    }

    @Test
    public void getCandidates_emptyCell_returnsValidNumbers() {
        int[][] grid = new int[9][9];
        grid[0][0] = 1;
        grid[0][1] = 2;
        List<Integer> candidates = GameValidator.getCandidates(grid, 0, 2);
        assertFalse(candidates.contains(1));
        assertFalse(candidates.contains(2));
        assertTrue(candidates.contains(3));
    }

    @Test
    public void getCandidates_emptyGrid_returnsAll9() {
        int[][] grid = new int[9][9];
        List<Integer> candidates = GameValidator.getCandidates(grid, 4, 4);
        assertEquals(9, candidates.size());
    }

    @Test
    public void getCandidates_almostFull_returnsSingleCandidate() {
        int[][] grid = createSolvedGrid();
        int correctValue = grid[4][4];
        grid[4][4] = 0;
        List<Integer> candidates = GameValidator.getCandidates(grid, 4, 4);
        assertTrue("Should contain the correct value", candidates.contains(correctValue));
    }

    // ═══════════════════════════════════════════════════════════════
    // String conversion
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void stringToGrid_validInput_parsesCorrectly() {
        String s = "123456789" + "0".repeat(72);
        int[][] grid = GameValidator.stringToGrid(s);
        assertEquals(1, grid[0][0]);
        assertEquals(9, grid[0][8]);
        assertEquals(0, grid[1][0]);
    }

    @Test
    public void gridToString_produces81Chars() {
        int[][] grid = new int[9][9];
        String s = GameValidator.gridToString(grid);
        assertEquals(81, s.length());
    }

    @Test
    public void isValidMove_outOfBounds_returnsFalse() {
        assertFalse(GameValidator.isValidMove(new int[9][9], -1, 0, 1));
        assertFalse(GameValidator.isValidMove(new int[9][9], 0, 9, 1));
        assertFalse(GameValidator.isValidMove(new int[9][9], 0, 0, 10));
    }

    @Test
    public void getCandidates_corruptValue_doesNotThrow() {
        int[][] grid = new int[9][9];
        grid[0][0] = 12;

        List<Integer> candidates = GameValidator.getCandidates(grid, 0, 1);

        assertEquals(9, candidates.size());
    }

    @Test
    public void stringToGrid_corruptInput_returnsEmptyGrid() {
        int[][] grid = GameValidator.stringToGrid("1A3456789" + "0".repeat(72));

        assertEquals(0, grid[0][0]);
        assertEquals(0, grid[0][2]);
    }

    @Test
    public void stringToGrid_shortInput_returnsEmptyGrid() {
        int[][] grid = GameValidator.stringToGrid("123");

        assertEquals(0, grid[0][0]);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private int[][] createTestGrid() {
        String s = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        return GameValidator.stringToGrid(s);
    }

    private int[][] createSolvedGrid() {
        int[][] grid = createTestGrid();
        new SudokuSolver().solve(grid);
        return grid;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 9);
        }
        return copy;
    }
}
