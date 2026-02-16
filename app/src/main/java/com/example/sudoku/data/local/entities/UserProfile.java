package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User profile containing all player data.
 * Only ONE row per user (single player game).
 */
@Entity(tableName = "user_profile")
public class UserProfile {
    
    @PrimaryKey
    @NonNull
    public String userId;           // UUID generated on first launch
    
    public int currentLevel;        // Current unlocked level (1-999)
    public int currentXP;           // Experience points
    public int totalXP;             // Lifetime XP (never decreases)
    
    public int currentStreak;       // Consecutive days played
    public int longestStreak;       // Best streak ever
    public long lastPlayedDate;     // Timestamp of last game completion
    
    // Statistics
    public int totalGamesPlayed;
    public int totalGamesWon;
    public int totalGamesLost;      // From mistakes or giving up
    public int totalStarsEarned;
    public int totalHintsUsed;
    public long totalPlayTimeMs;    // Cumulative play time
    
    // Settings
    public boolean soundEnabled;
    public boolean vibrationEnabled;
    public String theme;            // "dark", "light", "blue", etc.
    
    // Grace period for streak protection
    public int graceUsesThisMonth;  // Reset monthly
    public String graceResetMonth;  // "2025-02" format
    
    // Timestamps
    public long createdAt;
    public long updatedAt;
    
    public UserProfile(@NonNull String userId) {
        this.userId = userId;
        this.currentLevel = 1;
        this.currentXP = 0;
        this.totalXP = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastPlayedDate = 0;
        this.totalGamesPlayed = 0;
        this.totalGamesWon = 0;
        this.totalGamesLost = 0;
        this.totalStarsEarned = 0;
        this.totalHintsUsed = 0;
        this.totalPlayTimeMs = 0;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        this.theme = "dark";
        this.graceUsesThisMonth = 0;
        this.graceResetMonth = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
