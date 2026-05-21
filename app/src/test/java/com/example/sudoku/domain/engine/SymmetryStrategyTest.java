package com.example.sudoku.domain.engine;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class SymmetryStrategyTest {

    @Test
    public void none_yieldsSelf() {
        List<int[]> partners = SymmetryStrategy.NONE.partners(3, 4);
        assertEquals(1, partners.size());
        assertArrayEquals(new int[]{3, 4}, partners.get(0));
    }

    @Test
    public void rotational180_yieldsMirroredCell() {
        List<int[]> partners = SymmetryStrategy.ROTATIONAL_180.partners(2, 5);
        assertEquals(2, partners.size());
        assertArrayEquals(new int[]{2, 5}, partners.get(0));
        assertArrayEquals(new int[]{6, 3}, partners.get(1));
    }

    @Test
    public void rotational180_centerIsSingleCell() {
        List<int[]> partners = SymmetryStrategy.ROTATIONAL_180.partners(4, 4);
        assertEquals(1, partners.size());
    }

    @Test
    public void rotational90_producesFourCellOrbit() {
        List<int[]> partners = SymmetryStrategy.ROTATIONAL_90.partners(0, 0);
        assertEquals(4, partners.size());
    }

    @Test
    public void diagonal_includesTranspose() {
        List<int[]> partners = SymmetryStrategy.DIAGONAL.partners(1, 7);
        assertEquals(2, partners.size());
        assertArrayEquals(new int[]{7, 1}, partners.get(1));
    }
}
