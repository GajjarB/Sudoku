package com.example.sudoku.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for GridUtils: parsing, edge cases, invalid input.
 */
public class GridUtilsTest {

    @Test
    public void parseGrid_validString_parsesCorrectly() {
        String s = "123456789" + "0".repeat(72);
        int[][] grid = GridUtils.parseGrid(s);
        assertEquals(1, grid[0][0]);
        assertEquals(9, grid[0][8]);
        assertEquals(0, grid[1][0]);
    }

    @Test
    public void parseGrid_allZeros_returnsEmptyGrid() {
        String s = "0".repeat(81);
        int[][] grid = GridUtils.parseGrid(s);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                assertEquals(0, grid[r][c]);
            }
        }
    }

    @Test
    public void parseGrid_nullString_returnsEmptyGrid() {
        int[][] grid = GridUtils.parseGrid(null);
        assertNotNull(grid);
        assertEquals(9, grid.length);
        assertEquals(0, grid[0][0]);
    }

    @Test
    public void parseGrid_shortString_returnsEmptyGrid() {
        int[][] grid = GridUtils.parseGrid("12345");
        assertEquals(0, grid[0][0]); // Should return empty grid
    }

    @Test
    public void parseGrid_longString_returnsEmptyGrid() {
        int[][] grid = GridUtils.parseGrid("0".repeat(100));
        assertEquals(0, grid[0][0]); // Should return empty grid (length != 81)
    }

    @Test
    public void parseGrid_withDots_treatsAsZero() {
        // Dots are non-digit, so should map to 0
        String s = "1.3456789" + "0".repeat(72);
        int[][] grid = GridUtils.parseGrid(s);
        assertEquals(1, grid[0][0]);
        assertEquals(0, grid[0][1]); // '.' → 0
        assertEquals(3, grid[0][2]);
    }

    @Test
    public void parseGrid_withCorruptCharacter_returnsEmptyGrid() {
        String s = "1A3456789" + "0".repeat(72);

        int[][] grid = GridUtils.parseGrid(s);

        assertEquals(0, grid[0][0]);
        assertEquals(0, grid[0][2]);
    }
}
