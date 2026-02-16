package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Complete game state for save/resume functionality.
 * Critical for player retention.
 */
@Entity(
    tableName = "saved_games",
    foreignKeys = @ForeignKey(
        entity = LevelProgress.class,
        parentColumns = "levelNumber",
        childColumns = "levelNumber",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("levelNumber"), @Index("isCompleted")}
)
public class SavedGame {
    
    @PrimaryKey
    @NonNull
    public String gameId;           // UUID for this game instance
    
    public int levelNumber;         // Which level this is
    
    // Grid state (81 characters, '0' = empty, '1'-'9' = filled)
    @NonNull
    public String puzzleGrid;       // Original puzzle with clues
    @NonNull
    public String solutionGrid;     // Complete solution
    @NonNull
    public String currentGrid;      // Player's current state
    @NonNull
    public String lockedCells;      // '1' = locked (original clue), '0' = editable
    
    // Game progress
    public long elapsedTimeMs;      // Time spent on this game
    public int hintsRemaining;      // Hints left (max 3)
    public int mistakesMade;        // Errors made (max 3)
    public boolean isCompleted;     // Game finished?
    public boolean isDailyChallenge;// Is this the daily challenge?
    
    // Timestamps
    public long startedAt;
    public long lastPlayedAt;       // Renamed from lastSavedAt to match usage if preferred, OR keep lastSavedAt
    public long completedAt;
    
    public SavedGame(@NonNull String gameId, int levelNumber,
                     @NonNull String puzzleGrid, @NonNull String solutionGrid,
                     @NonNull String lockedCells) {
        this.gameId = gameId;
        this.levelNumber = levelNumber;
        this.puzzleGrid = puzzleGrid;
        this.solutionGrid = solutionGrid;
        this.currentGrid = puzzleGrid; // Start with puzzle state
        this.lockedCells = lockedCells;
        this.elapsedTimeMs = 0;
        this.hintsRemaining = 3;
        this.mistakesMade = 0;
        this.isCompleted = false;
        this.isDailyChallenge = false;
        this.startedAt = System.currentTimeMillis();
        this.lastPlayedAt = System.currentTimeMillis();
        this.completedAt = 0;
    }
}
