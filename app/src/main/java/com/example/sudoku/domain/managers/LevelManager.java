package com.example.sudoku.domain.managers;

import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;

/**
 * Manages level progression, XP awards, and unlocking logic.
 */
public class LevelManager {
    
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    
    public LevelManager(LevelRepository levelRepository, UserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
    }
    
    public void onLevelCompleted(int levelNumber, int score, long timeMs, int difficulty) {
        // Calculate stars based on score
        int stars = calculateStars(score, difficulty);
        
        // Update level progress
        levelRepository.completeLevel(levelNumber, stars, score, timeMs);
        
        // Award XP to user
        int xpEarned = calculateXP(score);
        // We need to get current user XP to update it. 
        // Note: In a real app, we'd observe the user profile or fetch it.
        // For now, this logic assumes we have a way to increment XP transactionally or via the data layer.
        // Simplified:
        // userRepository.addXP(xpEarned); 
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
    
    public int getNextLevelUnlock(int currentLevel) {
        return currentLevel + 1;
    }
}
