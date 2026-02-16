package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Historical record of completed games.
 * Used for statistics and AI learning.
 */
@Entity(
    tableName = "game_history",
    foreignKeys = @ForeignKey(
        entity = SavedGame.class,
        parentColumns = "gameId",
        childColumns = "gameId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("gameId"), @Index("playedAt")}
)
public class GameHistory {
    
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String gameId;           // Reference to SavedGame
    
    public int levelNumber;
    public long playedAt;           // Completion timestamp
    
    // Performance metrics
    public long totalTimeMs;        // Time taken to complete
    public int finalScore;
    public int xpEarned;
    public int starsEarned;         // 1-3 stars
    
    public int hintsUsed;
    public int mistakesMade;
    
    // AI Analysis Data
    public double avgDecisionTimeMs;
    public double accuracyRate;     // % of correct first attempts
    
    public boolean isDailyChallenge;
    public boolean isPerfectGame;   // No mistakes, no hints
}
