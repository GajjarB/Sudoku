═══════════════════════════════════════════════════════════════════════════════
PART 2: DATA ACCESS LAYER - DAOs & DATABASE SETUP
═══════════════════════════════════════════════════════════════════════════════

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE: data/local/dao/GameDao.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

package com.example.sudoku.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.sudoku.data.local.entities.*;
import java.util.List;

@Dao
public interface GameDao {
    
    // ═══════════════════════════════════════════════════════════════
    // SAVED GAMES
    // ═══════════════════════════════════════════════════════════════
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSavedGame(SavedGame game);
    
    @Update
    void updateSavedGame(SavedGame game);
    
    @Query("SELECT * FROM saved_games WHERE gameId = :gameId")
    LiveData<SavedGame> getSavedGameLive(String gameId);
    
    @Query("SELECT * FROM saved_games WHERE gameId = :gameId")
    SavedGame getSavedGame(String gameId);
    
    @Query("SELECT * FROM saved_games WHERE isCompleted = 0 ORDER BY lastSavedAt DESC LIMIT 1")
    SavedGame getIncompleteGame();
    
    @Query("SELECT * FROM saved_games WHERE isCompleted = 0 ORDER BY lastSavedAt DESC LIMIT 1")
    LiveData<SavedGame> getIncompleteGameLive();
    
    @Delete
    void deleteSavedGame(SavedGame game);
    
    @Query("DELETE FROM saved_games WHERE isCompleted = 1 AND completedAt < :cutoffTime")
    void deleteOldCompletedGames(long cutoffTime);
    
    // ═══════════════════════════════════════════════════════════════
    // GAME HISTORY
    // ═══════════════════════════════════════════════════════════════
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGameHistory(GameHistory history);
    
    @Query("SELECT * FROM game_history ORDER BY playedAt DESC LIMIT :limit")
    List<GameHistory> getRecentGames(int limit);
    
    @Query("SELECT * FROM game_history WHERE playedAt >= :since ORDER BY playedAt DESC")
    List<GameHistory> getGamesSince(long since);
    
    @Query("SELECT COUNT(*) FROM game_history WHERE isDailyChallenge = 1")
    int getTotalDailyChallengesCompleted();
    
    @Query("SELECT AVG(totalTimeMs) FROM game_history WHERE playedAt >= :since")
    double getAverageCompletionTime(long since);
    
    // ═══════════════════════════════════════════════════════════════
    // PLAYER BEHAVIOR (AI Analytics)
    // ═══════════════════════════════════════════════════════════════
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayerBehavior(PlayerBehavior behavior);
    
    @Query("SELECT * FROM player_behavior WHERE timestamp >= :since ORDER BY timestamp DESC")
    List<PlayerBehavior> getBehaviorSince(long since);
    
    @Query("SELECT * FROM player_behavior WHERE strategyUsed = :strategy ORDER BY timestamp DESC LIMIT :limit")
    List<PlayerBehavior> getRecentBehaviorsByStrategy(String strategy, int limit);
    
    @Query("DELETE FROM player_behavior WHERE timestamp < :cutoffTime")
    void deleteOldBehaviorData(long cutoffTime);
    
    // ═══════════════════════════════════════════════════════════════
    // SOLVING PATTERNS (AI Learning)
    // ═══════════════════════════════════════════════════════════════
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSolvingPattern(SolvingPattern pattern);
    
    @Query("SELECT * FROM solving_patterns WHERE patternType = :type")
    SolvingPattern getSolvingPattern(String type);
    
    @Query("SELECT * FROM solving_patterns ORDER BY confidenceScore ASC")
    List<SolvingPattern> getAllSolvingPatterns();
    
    // ═══════════════════════════════════════════════════════════════
    // COGNITIVE METRICS
    // ═══════════════════════════════════════════════════════════════
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCognitiveMetrics(CognitiveMetrics metrics);
    
    @Query("SELECT * FROM cognitive_metrics ORDER BY dateKey DESC LIMIT 1")
    CognitiveMetrics getLatestCognitiveMetrics();
    
    @Query("SELECT * FROM cognitive_metrics WHERE dateKey = :date")
    CognitiveMetrics getCognitiveMetrics(String date);
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE: data/local/dao/UserProfileDao.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

package com.example.sudoku.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.sudoku.data.local.entities.UserProfile;

@Dao
public interface UserProfileDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserProfile(UserProfile profile);
    
    @Update
    void updateUserProfile(UserProfile profile);
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getUserProfile();
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    LiveData<UserProfile> getUserProfileLive();
    
    @Query("UPDATE user_profile SET currentXP = :xp, totalXP = :totalXp, currentLevel = :level WHERE userId = :userId")
    void updateXPAndLevel(String userId, int xp, int totalXp, int level);
    
    @Query("UPDATE user_profile SET currentStreak = :streak, longestStreak = :longest WHERE userId = :userId")
    void updateStreak(String userId, int streak, int longest);
    
    @Query("UPDATE user_profile SET soundEnabled = :enabled WHERE userId = :userId")
    void updateSoundEnabled(String userId, boolean enabled);
    
    @Query("UPDATE user_profile SET vibrationEnabled = :enabled WHERE userId = :userId")
    void updateVibrationEnabled(String userId, boolean enabled);
    
    @Query("UPDATE user_profile SET theme = :theme WHERE userId = :userId")
    void updateTheme(String userId, String theme);
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE: data/local/dao/LevelProgressDao.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

package com.example.sudoku.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.sudoku.data.local.entities.LevelProgress;
import java.util.List;

@Dao
public interface LevelProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLevelProgress(LevelProgress progress);
    
    @Update
    void updateLevelProgress(LevelProgress progress);
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<LevelProgress> levels);
    
    @Query("SELECT * FROM level_progress WHERE levelNumber = :level")
    LevelProgress getLevelProgress(int level);
    
    @Query("SELECT * FROM level_progress WHERE levelNumber = :level")
    LiveData<LevelProgress> getLevelProgressLive(int level);
    
    @Query("SELECT * FROM level_progress WHERE isUnlocked = 1 ORDER BY levelNumber ASC")
    LiveData<List<LevelProgress>> getUnlockedLevels();
    
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC LIMIT :limit")
    List<LevelProgress> getLevels(int limit);
    
    @Query("UPDATE level_progress SET isUnlocked = 1 WHERE levelNumber = :level")
    void unlockLevel(int level);
    
    @Query("UPDATE level_progress SET isCompleted = 1, starsEarned = :stars, bestScore = :score, bestTimeMs = :time, firstCompletedAt = :timestamp WHERE levelNumber = :level AND isCompleted = 0")
    void completeLevel(int level, int stars, int score, long time, long timestamp);
    
    @Query("UPDATE level_progress SET starsEarned = :stars, bestScore = :score, bestTimeMs = :time WHERE levelNumber = :level AND (bestScore < :score OR (bestScore = :score AND bestTimeMs > :time))")
    void updateBestPerformance(int level, int stars, int score, long time);
    
    @Query("SELECT COUNT(*) FROM level_progress WHERE isCompleted = 1")
    int getTotalCompletedLevels();
    
    @Query("SELECT SUM(starsEarned) FROM level_progress WHERE isCompleted = 1")
    int getTotalStars();
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE: data/local/dao/DailyChallengeDao.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

package com.example.sudoku.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.sudoku.data.local.entities.DailyChallenge;

@Dao
public interface DailyChallengeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDailyChallenge(DailyChallenge challenge);
    
    @Update
    void updateDailyChallenge(DailyChallenge challenge);
    
    @Query("SELECT * FROM daily_challenge WHERE dateKey = :date")
    DailyChallenge getDailyChallenge(String date);
    
    @Query("SELECT * FROM daily_challenge WHERE dateKey = :date")
    LiveData<DailyChallenge> getDailyChallengeLive(String date);
    
    @Query("DELETE FROM daily_challenge WHERE expiresAt < :timestamp")
    void deleteExpiredChallenges(long timestamp);
    
    @Query("SELECT COUNT(*) FROM daily_challenge WHERE isCompleted = 1")
    int getTotalDailyChallengesCompleted();
}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FILE: data/local/database/SudokuDatabase.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

package com.example.sudoku.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
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


═══════════════════════════════════════════════════════════════════════════════
END OF PART 2
═══════════════════════════════════════════════════════════════════════════════

All DAO interfaces and database setup are now complete.

Next sections will include:
- Repositories (GameRepository, UserRepository, etc.)
- Preferences Manager
- Domain Models
- Game Engine (SudokuGenerator, GameValidator, SudokuSolver)
- AI System (BehavioralAnalyzer, IntelligentPuzzleGenerator, etc.)
- All remaining components

Would you like me to continue with the next parts?
