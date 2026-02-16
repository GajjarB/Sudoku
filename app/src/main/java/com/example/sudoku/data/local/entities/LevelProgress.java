package com.example.sudoku.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Progress tracking for each level (1-999).
 * One row per level.
 */
@Entity(tableName = "level_progress")
public class LevelProgress {
    
    @PrimaryKey
    public int levelNumber;         // 1 to 999
    
    public boolean isUnlocked;      // Can player access this level?
    public boolean isCompleted;     // Has player beaten it?
    
    public int starsEarned;         // 0-3 stars
    public int bestScore;           // Highest score achieved
    public long bestTimeMs;         // Fastest completion time
    
    public int timesPlayed;         // How many attempts
    public int timesCompleted;      // How many wins
    
    public long firstCompletedAt;   // Timestamp of first win
    public long lastPlayedAt;       // Timestamp of last attempt
    
    public LevelProgress(int levelNumber) {
        this.levelNumber = levelNumber;
        this.isUnlocked = (levelNumber == 1); // Only level 1 unlocked initially
        this.isCompleted = false;
        this.starsEarned = 0;
        this.bestScore = 0;
        this.bestTimeMs = Long.MAX_VALUE;
        this.timesPlayed = 0;
        this.timesCompleted = 0;
        this.firstCompletedAt = 0;
        this.lastPlayedAt = 0;
    }
}
