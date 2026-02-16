package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Behavioral analytics - tracks every cell interaction.
 * Used by AI to learn player patterns.
 */
@Entity(
    tableName = "player_behavior",
    indices = {@Index("sessionId"), @Index("timestamp")}
)
public class PlayerBehavior {
    
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String sessionId;        // Game ID
    public int levelNumber;
    public long timestamp;
    
    // Cell interaction data
    public int row;
    public int col;
    public int numberPlaced;
    public boolean wasCorrect;
    public long thinkingTimeMs;     // How long before placing
    public int changeCount;         // How many times cell was edited
    
    // Strategy detection
    public String strategyUsed;     // NAKED_SINGLE, HIDDEN_PAIR, etc.
    
    // Context
    public int emptyCellsRemaining;
    public double gridComplexity;   // 0-1 score
}
