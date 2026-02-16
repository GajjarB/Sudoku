package com.example.sudoku.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.sudoku.data.local.dao.*;
import com.example.sudoku.data.local.entities.*;
import com.example.sudoku.utils.Constants;

/**
 * Main database class for Sudoku Nexus.
 * Singleton pattern ensures only one instance exists.
 */
@Database(
    entities = {
        UserProfile.class,
        LevelProgress.class,
        SavedGame.class,
        GameHistory.class,
        DailyChallenge.class,
        PlayerBehavior.class,
        SolvingPattern.class,
        CognitiveMetrics.class
    },
    version = Constants.DATABASE_VERSION,
    exportSchema = true
)
public abstract class SudokuDatabase extends RoomDatabase {
    
    private static volatile SudokuDatabase INSTANCE;
    
    // DAO getters
    public abstract GameDao gameDao();
    public abstract UserProfileDao userProfileDao();
    public abstract LevelProgressDao levelProgressDao();
    public abstract DailyChallengeDao dailyChallengeDao();
    
    /**
     * Get database instance (thread-safe singleton).
     */
    public static SudokuDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SudokuDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        SudokuDatabase.class,
                        Constants.DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // For development only!
                    // .addMigrations(MIGRATION_1_2) // Add proper migrations for production
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Reset database instance (for testing only).
     */
    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}
