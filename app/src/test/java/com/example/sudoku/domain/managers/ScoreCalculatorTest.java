package com.example.sudoku.domain.managers;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for ScoreCalculator: score formula, XP calculation, star thresholds.
 */
public class ScoreCalculatorTest {

    private ScoreCalculator calculator;

    @Before
    public void setUp() {
        calculator = new ScoreCalculator();
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateScore
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateScore_perfectGame_highScore() {
        // Level 1, 60 seconds, no mistakes, no hints
        int score = calculator.calculateScore(1, 60_000, 0, 0);
        assertEquals("Base 1000 - 60s penalty = 940", 940, score);
    }

    @Test
    public void calculateScore_withMistakes_penalized() {
        int noMistakes = calculator.calculateScore(1, 60_000, 0, 0);
        int withMistakes = calculator.calculateScore(1, 60_000, 2, 0);
        assertTrue("Mistakes should reduce score", withMistakes < noMistakes);
        assertEquals(noMistakes - 100, withMistakes); // 2 * 50 = 100 penalty
    }

    @Test
    public void calculateScore_withHints_penalized() {
        int noHints = calculator.calculateScore(1, 60_000, 0, 0);
        int withHints = calculator.calculateScore(1, 60_000, 0, 2);
        assertTrue("Hints should reduce score", withHints < noHints);
        assertEquals(noHints - 200, withHints); // 2 * 100 = 200 penalty
    }

    @Test
    public void calculateScore_highDifficulty_higherBase() {
        int level1 = calculator.calculateScore(1, 60_000, 0, 0);
        int level5 = calculator.calculateScore(5, 60_000, 0, 0);
        assertTrue("Higher difficulty should give higher base score", level5 > level1);
    }

    @Test
    public void calculateScore_minimumScore_is50() {
        // Very long time + max mistakes + max hints → should floor at 50
        int score = calculator.calculateScore(1, 3600_000, 3, 3); // 1 hour
        assertEquals("Score should never go below 50", 50, score);
    }

    @Test
    public void calculateScore_timePenalty_onePointPerSecond() {
        int fast = calculator.calculateScore(1, 100_000, 0, 0);   // 100s
        int slow = calculator.calculateScore(1, 200_000, 0, 0);   // 200s
        assertEquals("100s difference = 100 point difference", 100, fast - slow);
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateXP
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateXP_tenPercentOfScore() {
        assertEquals(100, calculator.calculateXP(1000));
        assertEquals(50, calculator.calculateXP(500));
        assertEquals(0, calculator.calculateXP(0));
    }

    // ═══════════════════════════════════════════════════════════════
    // calculateStars
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void calculateStars_highScore_3stars() {
        // Level 1 baseTarget = 1000, 90% = 900
        assertEquals(3, calculator.calculateStars(950, 1));
    }

    @Test
    public void calculateStars_mediumScore_2stars() {
        // 60% of 1000 = 600
        assertEquals(2, calculator.calculateStars(700, 1));
    }

    @Test
    public void calculateStars_lowScore_1star() {
        assertEquals(1, calculator.calculateStars(500, 1));
    }

    @Test
    public void calculateStars_exactThreshold90_3stars() {
        // Exactly 90% of baseTarget
        assertEquals(3, calculator.calculateStars(900, 1));
    }

    @Test
    public void calculateStars_exactThreshold60_2stars() {
        assertEquals(2, calculator.calculateStars(600, 1));
    }

    @Test
    public void calculateStars_belowMinimum_1star() {
        assertEquals(1, calculator.calculateStars(50, 1));
    }
}
