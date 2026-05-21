package com.example.sudoku.domain.managers;

import android.util.Log;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;

/**
 * Manages level progression, XP awards, and unlocking logic.
 */
public class LevelManager {

    private static final String TAG = "LevelManager";

    private final LevelRepository levelRepository;
    private final UserRepository userRepository;

    public LevelManager(LevelRepository levelRepository, UserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
    }

    public void onLevelCompleted(String userId, int levelNumber, int score, long timeMs, int difficulty) {
        int stars = calculateStars(score, difficulty);
        int xpEarned = calculateXP(score);

        // Update level progress
        levelRepository.completeLevel(levelNumber, stars, score, timeMs);

        // Award XP to user — synchronous, no persistent observer
        userRepository.awardXPSync(userId, xpEarned);
        Log.d(TAG, "XP awarded: +" + xpEarned);
    }

    public int calculateStars(int score, int difficulty) {
        int baseTarget = 1000 * difficulty;
        if (score >= baseTarget * 0.9) return 3;
        if (score >= baseTarget * 0.6) return 2;
        return 1;
    }

    public int calculateXP(int score) {
        return score / 10;
    }

    /**
     * Calculate user level from total XP using a curve.
     * Level formula: totalXP = baseXP * level^1.5
     */
    public int calculateUserLevel(int totalXP) {
        if (totalXP <= 0) return 1;
        // Inverse of XP curve: level = (totalXP / baseXP) ^ (1/1.5)
        double level = Math.pow(totalXP / 100.0, 1.0 / 1.5);
        return Math.max(1, (int) level);
    }

    public int getNextLevelUnlock(int currentLevel) {
        return currentLevel + 1;
    }
}
