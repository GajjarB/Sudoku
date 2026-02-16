package com.example.sudoku.domain.managers;

/**
 * Calculates score based on difficulty, time, and mistakes.
 */
public class ScoreCalculator {

    public int calculateScore(int difficultyLevel, long timeMs, int mistakes, int hintsUsed) {
        // Base score based on difficulty
        int baseScore = 1000 * difficultyLevel; // e.g. Level 1 = 1000, Level 5 = 5000
        
        // Time penalty (subtract 1 point per second)
        int timePenalty = (int) (timeMs / 1000);
        
        // Mistake penalty (big penalty)
        int mistakePenalty = mistakes * 50;
        
        // Hint penalty
        int hintPenalty = hintsUsed * 100;
        
        int finalScore = baseScore - timePenalty - mistakePenalty - hintPenalty;
        return Math.max(finalScore, 50); // Minimum 50 points
    }
    
    public int calculateXP(int score) {
        return score / 10; // 10% of score is XP
    }
    
    public int calculateStars(int score, int difficultyLevel) {
        int baseTarget = 1000 * difficultyLevel;
        if (score >= baseTarget * 0.9) return 3;
        if (score >= baseTarget * 0.6) return 2;
        return 1;
    }
}
