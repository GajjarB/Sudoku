package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tracks player proficiency with each Sudoku solving strategy.
 * AI uses this to identify weak areas.
 */
@Entity(tableName = "solving_patterns")
public class SolvingPattern {
    
    @PrimaryKey
    @NonNull
    public String patternType;      // Strategy name
    
    public int successCount;        // Times used correctly
    public int failureCount;        // Times used incorrectly
    public double avgTimeSeconds;   // Average execution time
    public double confidenceScore;  // 0-1, player mastery level
    public long lastUsed;           // Timestamp
    
    // Learning metrics
    public double learningRate;     // Improvement velocity
    public int consecutiveSuccess;
    public int consecutiveFailure;
    
    public SolvingPattern(@NonNull String patternType) {
        this.patternType = patternType;
        this.successCount = 0;
        this.failureCount = 0;
        this.avgTimeSeconds = 0;
        this.confidenceScore = 0.5;
        this.lastUsed = 0;
        this.learningRate = 0;
        this.consecutiveSuccess = 0;
        this.consecutiveFailure = 0;
    }
}
