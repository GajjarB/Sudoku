package com.example.sudoku.domain.ai;

import android.util.Log;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.utils.Constants;

import java.util.List;
import java.security.SecureRandom;

/**
 * DIFFICULTY ADAPTER
 * 
 * Analyzes recent performance and determines the optimal clue count.
 * Simpler fallback for basic adaptive difficulty.
 */
public class DifficultyAdapter {
    
    private static final String TAG = "DifficultyAdapter";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private GameDao dao;
    
    public DifficultyAdapter(GameDao dao) {
        this.dao = dao;
    }
    
    public int calculateOptimalClueCount(int levelNumber) {
        List<GameHistory> recentGames = dao.getRecentGames(10);
        
        if (recentGames.isEmpty()) {
            return Constants.CLUES_EASY_MIN;
        }
        
        double avgAccuracy = calculateAverageAccuracy(recentGames);
        double avgTime = calculateAverageTime(recentGames);
        int avgStars = calculateAverageStars(recentGames);
        
        int skillScore = calculateSkillScore(avgAccuracy, avgTime, avgStars);
        int clueCount = mapSkillToClues(skillScore);
        
        Log.d(TAG, String.format("Skill Analysis: Accuracy=%.2f, AvgTime=%.1fs, Stars=%.1f → Score=%d, Clues=%d",
                avgAccuracy, avgTime / 1000.0, (double) avgStars, skillScore, clueCount));
        
        return clueCount;
    }
    
    private double calculateAverageAccuracy(List<GameHistory> games) {
        if (games.isEmpty()) return 0.5;
        return games.stream()
                .mapToDouble(g -> g.accuracyRate)
                .average()
                .orElse(0.5);
    }
    
    private double calculateAverageTime(List<GameHistory> games) {
        if (games.isEmpty()) return 10 * 60 * 1000;
        return games.stream()
                .mapToLong(g -> g.totalTimeMs)
                .average()
                .orElse(10 * 60 * 1000);
    }
    
    private int calculateAverageStars(List<GameHistory> games) {
        if (games.isEmpty()) return 1;
        return (int) Math.round(games.stream()
                .mapToInt(g -> g.starsEarned)
                .average()
                .orElse(1.0));
    }
    
    private int calculateSkillScore(double accuracy, double avgTimeMs, int avgStars) {
        int score = 0;
        score += (int) (accuracy * 100);
        
        if (avgTimeMs < 3 * 60 * 1000) {
            score += 50;
        } else if (avgTimeMs < 6 * 60 * 1000) {
            score += 25;
        }
        
        if (avgStars >= 3) score += 50;
        else if (avgStars >= 2) score += 30;
        else score += 10;
        
        return Math.max(0, Math.min(200, score));
    }
    
    private int mapSkillToClues(int skillScore) {
        if (skillScore >= 160) {
            return Constants.CLUES_EXPERT_MIN + 
                   secureRandom.nextInt(Constants.CLUES_EXPERT_MAX - Constants.CLUES_EXPERT_MIN + 1);
        } else if (skillScore >= 120) {
            return Constants.CLUES_HARD_MIN +
                   secureRandom.nextInt(Constants.CLUES_HARD_MAX - Constants.CLUES_HARD_MIN + 1);
        } else if (skillScore >= 80) {
            return Constants.CLUES_MEDIUM_MIN +
                   secureRandom.nextInt(Constants.CLUES_MEDIUM_MAX - Constants.CLUES_MEDIUM_MIN + 1);
        } else {
            return Constants.CLUES_EASY_MIN +
                   secureRandom.nextInt(Constants.CLUES_EASY_MAX - Constants.CLUES_EASY_MIN + 1);
        }
    }
}
