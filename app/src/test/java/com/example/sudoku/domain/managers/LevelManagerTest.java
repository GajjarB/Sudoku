package com.example.sudoku.domain.managers;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for LevelManager: star calculation, XP computation, user level formula.
 */
public class LevelManagerTest {

    private LevelManager levelManager;

    @Before
    public void setUp() {
        // LevelManager needs repos for onLevelCompleted, but calculation methods are pure
        levelManager = new LevelManager(null, null);
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateStars
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateStars_above90Percent_3stars() {
        assertEquals(3, levelManager.calculateStars(950, 1)); // 95% of 1000
    }

    @Test
    public void calculateStars_at90Percent_3stars() {
        assertEquals(3, levelManager.calculateStars(900, 1)); // Exactly 90%
    }

    @Test
    public void calculateStars_between60And90_2stars() {
        assertEquals(2, levelManager.calculateStars(700, 1));
    }

    @Test
    public void calculateStars_below60Percent_1star() {
        assertEquals(1, levelManager.calculateStars(400, 1));
    }

    @Test
    public void calculateStars_higherDifficulty_adjustsThresholds() {
        // Difficulty 3: baseTarget = 3000, 90% = 2700
        assertEquals(3, levelManager.calculateStars(2800, 3));
        assertEquals(2, levelManager.calculateStars(2000, 3));
        assertEquals(1, levelManager.calculateStars(1000, 3));
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateXP
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateXP_dividesScoreBy10() {
        assertEquals(100, levelManager.calculateXP(1000));
        assertEquals(0, levelManager.calculateXP(5)); // Integer division rounds down
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateUserLevel
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateUserLevel_zeroXP_level1() {
        assertEquals(1, levelManager.calculateUserLevel(0));
    }

    @Test
    public void calculateUserLevel_negativeXP_level1() {
        assertEquals(1, levelManager.calculateUserLevel(-100));
    }

    @Test
    public void calculateUserLevel_increasesWithXP() {
        int lowLevel = levelManager.calculateUserLevel(500);
        int highLevel = levelManager.calculateUserLevel(5000);
        assertTrue("More XP should mean higher level", highLevel > lowLevel);
    }

    @Test
    public void calculateUserLevel_reasonableProgression() {
        // With 100 base XP, level ~1 at 100 XP, should grow gradually
        int level1 = levelManager.calculateUserLevel(100);
        int level10k = levelManager.calculateUserLevel(10000);
        assertTrue("Level at 100XP should be small", level1 <= 3);
        assertTrue("Level at 10000XP should be significant", level10k >= 5);
    }

    // ═══════════════════════════════════════════════════════════════
    // getNextLevelUnlock
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void getNextLevelUnlock_incrementsByOne() {
        assertEquals(2, levelManager.getNextLevelUnlock(1));
        assertEquals(100, levelManager.getNextLevelUnlock(99));
    }
}
