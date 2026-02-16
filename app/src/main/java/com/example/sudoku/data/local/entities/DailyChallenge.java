package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Daily challenge puzzle.
 * Same puzzle for all users on the same date.
 */
@Entity(tableName = "daily_challenge")
public class DailyChallenge {
    
    @PrimaryKey
    @NonNull
    public String dateKey;          // "2025-02-16" format
    
    @NonNull
    public String puzzleGrid;       // The puzzle for this date
    @NonNull
    public String solutionGrid;     // Solution
    
    public int clueCount;           // Difficulty indicator
    public long seed;               // Seed used for generation
    
    public boolean isCompleted;     // Has user completed it?
    public int bestScore;           // User's best score for this daily
    public long bestTimeMs;         // User's best time
    
    public long createdAt;          // When this challenge was generated
    public long expiresAt;          // Midnight of next day
    
    public DailyChallenge(@NonNull String dateKey) {
        this.dateKey = dateKey;
        this.puzzleGrid = "";
        this.solutionGrid = "";
        this.clueCount = 0;
        this.seed = 0;
        this.isCompleted = false;
        this.bestScore = 0;
        this.bestTimeMs = Long.MAX_VALUE;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = 0;
    }
}
