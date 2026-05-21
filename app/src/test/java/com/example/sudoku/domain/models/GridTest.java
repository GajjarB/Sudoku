package com.example.sudoku.domain.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Grid model: construction, cell access, bounds, toIntArray.
 */
public class GridTest {

    @Test
    public void constructor_createsCorrectGrid() {
        int[][] values = new int[9][9];
        values[0][0] = 5;
        values[4][4] = 9;
        boolean[][] locked = new boolean[9][9];
        locked[0][0] = true;

        Grid grid = new Grid(values, locked);
        assertEquals(5, grid.getCell(0, 0).getValue());
        assertTrue(grid.getCell(0, 0).isLocked());
        assertEquals(9, grid.getCell(4, 4).getValue());
        assertFalse(grid.getCell(4, 4).isLocked());
    }

    @Test
    public void getCell_validCoordinates_returnsCell() {
        Grid grid = createEmptyGrid();
        assertNotNull(grid.getCell(0, 0));
        assertNotNull(grid.getCell(8, 8));
        assertNotNull(grid.getCell(4, 4));
    }

    @Test
    public void getCell_outOfBounds_returnsNull() {
        Grid grid = createEmptyGrid();
        assertNull(grid.getCell(-1, 0));
        assertNull(grid.getCell(0, -1));
        assertNull(grid.getCell(9, 0));
        assertNull(grid.getCell(0, 9));
    }

    @Test
    public void toIntArray_matchesOriginalValues() {
        int[][] values = new int[9][9];
        values[0][0] = 1;
        values[8][8] = 9;
        boolean[][] locked = new boolean[9][9];

        Grid grid = new Grid(values, locked);
        int[][] result = grid.toIntArray();

        assertEquals(1, result[0][0]);
        assertEquals(9, result[8][8]);
        assertEquals(0, result[4][4]);
    }

    @Test
    public void toIntArray_reflectsModifiedCells() {
        Grid grid = createEmptyGrid();
        grid.getCell(3, 3).setValue(7);
        int[][] result = grid.toIntArray();
        assertEquals(7, result[3][3]);
    }

    private Grid createEmptyGrid() {
        return new Grid(new int[9][9], new boolean[9][9]);
    }
}
