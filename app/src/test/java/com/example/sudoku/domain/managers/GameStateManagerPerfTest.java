package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.domain.models.GameState;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Random;

public class GameStateManagerPerfTest {

    @Test
    public void testRefreshConflictsPerf() throws Exception {
        GameStateManager manager = new GameStateManager();

        // Setup a messy grid
        int[][] currentGrid = new int[9][9];
        int[][] solutionGrid = new int[9][9];
        boolean[][] lockedCells = new boolean[9][9];

        Random rand = new Random(42);
        for(int r = 0; r < 9; r++) {
            for(int c = 0; c < 9; c++) {
                solutionGrid[r][c] = (r + c) % 9 + 1; // dummy solution
                if (rand.nextBoolean()) {
                    lockedCells[r][c] = true;
                    currentGrid[r][c] = solutionGrid[r][c];
                } else {
                    currentGrid[r][c] = rand.nextInt(10); // randomly filled or empty
                }
            }
        }

        Field currentGridField = GameStateManager.class.getDeclaredField("currentGrid");
        currentGridField.setAccessible(true);
        currentGridField.set(manager, currentGrid);

        Field solutionGridField = GameStateManager.class.getDeclaredField("solutionGrid");
        solutionGridField.setAccessible(true);
        solutionGridField.set(manager, solutionGrid);

        Field lockedCellsField = GameStateManager.class.getDeclaredField("lockedCells");
        lockedCellsField.setAccessible(true);
        lockedCellsField.set(manager, lockedCells);

        Method refreshConflicts = GameStateManager.class.getDeclaredMethod("refreshConflicts");
        refreshConflicts.setAccessible(true);

        // Warm up
        for (int i = 0; i < 1000; i++) {
            refreshConflicts.invoke(manager);
        }

        long start = System.nanoTime();
        int iters = 10000;
        for (int i = 0; i < iters; i++) {
            refreshConflicts.invoke(manager);
        }
        long end = System.nanoTime();

        System.out.println("Average time per refreshConflicts: " + ((end - start) / iters) + " ns");
    }
}
