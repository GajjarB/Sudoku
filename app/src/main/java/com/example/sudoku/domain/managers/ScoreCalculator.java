package com.example.sudoku.domain.managers;

import com.example.sudoku.utils.Constants;

/**
 * Calculates score based on difficulty, time, and mistakes.
 */
public class ScoreCalculator {

    // Difficulty multipliers applied to Constants.BASE_SCORE
    private static final double MULTIPLIER_EASY   = 1.0; // levels   1–5
    private static final double MULTIPLIER_MEDIUM  = 1.5; // levels   6–20
    private static final double MULTIPLIER_HARD    = 2.0; // levels  21–50
    private static final double MULTIPLIER_EXPERT  = 3.0; // levels  51–200
    private static final double MULTIPLIER_MASTER  = 4.0; // levels 201–999

    // Time budgets (in ms) per difficulty bracket; no penalty while within budget
    private static final long BUDGET_EASY_MS   =  5 * 60 * 1000L;
    private static final long BUDGET_MEDIUM_MS = 10 * 60 * 1000L;
    private static final long BUDGET_HARD_MS   = 20 * 60 * 1000L;
    private static final long BUDGET_EXPERT_MS = 30 * 60 * 1000L;
    private static final long BUDGET_MASTER_MS = 45 * 60 * 1000L;

    /** Returns the difficulty multiplier for the given level. */
    private double difficultyMultiplier(int difficultyLevel) {
        if (difficultyLevel < 5)    return MULTIPLIER_EASY;
        if (difficultyLevel <= 20)  return MULTIPLIER_MEDIUM;
        if (difficultyLevel <= 50)  return MULTIPLIER_HARD;
        if (difficultyLevel <= 200) return MULTIPLIER_EXPERT;
        return MULTIPLIER_MASTER;
    }

    public int calculateScore(int difficultyLevel, long timeMs, int mistakes, int hintsUsed) {
        double multiplier = difficultyMultiplier(difficultyLevel);
        int baseScore = (int) (Constants.BASE_SCORE * multiplier);

        // Time penalty: 1 pt/sec
        int timePenalty = (int) (timeMs / 1000);

        int mistakePenalty = mistakes * Constants.MISTAKE_PENALTY;
        int hintPenalty    = hintsUsed * Constants.HINT_PENALTY;

        int finalScore = baseScore - timePenalty - mistakePenalty - hintPenalty;
        return Math.max(50, finalScore);
    }

    public int calculateXP(int score) {
        return score / 10; // 10% of score is XP
    }

    public int calculateStars(int score, int difficultyLevel) {
        // Star thresholds are based on absolute scores for Level 1.
        // For higher levels, they scale with the difficulty multiplier.
        double multiplier = difficultyMultiplier(difficultyLevel);
        int threshold3 = (int) (Constants.STARS_3_MIN_SCORE * multiplier);
        int threshold2 = (int) (Constants.STARS_2_MIN_SCORE * multiplier);
        if (score >= threshold3) return 3;
        if (score >= threshold2) return 2;
        return 1;
    }
}
