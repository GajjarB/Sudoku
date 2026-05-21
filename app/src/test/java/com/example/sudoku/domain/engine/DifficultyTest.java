package com.example.sudoku.domain.engine;

import org.junit.Test;
import static org.junit.Assert.*;

public class DifficultyTest {

    @Test
    public void fromClueCount_mapsExpectedRanges() {
        assertEquals(Difficulty.EASY, Difficulty.fromClueCount(40));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromClueCount(34));
        assertEquals(Difficulty.HARD, Difficulty.fromClueCount(30));
        assertEquals(Difficulty.EXPERT, Difficulty.fromClueCount(26));
        assertEquals(Difficulty.MASTER, Difficulty.fromClueCount(22));
        assertEquals(Difficulty.EXTREME, Difficulty.fromClueCount(18));
    }

    @Test
    public void targetClues_withinRange() {
        for (Difficulty d : Difficulty.values()) {
            int target = d.targetClues();
            assertTrue(d + " target " + target + " below min " + d.minClues(),
                    target >= d.minClues());
            assertTrue(d + " target " + target + " above max " + d.maxClues(),
                    target <= d.maxClues());
        }
    }

    @Test
    public void fromStrategyScore_picksHigherTier() {
        assertEquals(Difficulty.BEGINNER, Difficulty.fromStrategyScore(0.5));
        assertEquals(Difficulty.EASY, Difficulty.fromStrategyScore(2.0));
        assertEquals(Difficulty.HARD, Difficulty.fromStrategyScore(5.0));
        assertEquals(Difficulty.EXTREME, Difficulty.fromStrategyScore(50.0));
    }
}
