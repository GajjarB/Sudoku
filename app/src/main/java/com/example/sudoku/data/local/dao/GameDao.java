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
    
    @Query("SELECT * FROM saved_games WHERE isCompleted = 0 ORDER BY lastPlayedAt DESC LIMIT 1")
    SavedGame getIncompleteGame();
    
    @Query("SELECT * FROM saved_games WHERE isCompleted = 0 ORDER BY lastPlayedAt DESC LIMIT 1")
    LiveData<SavedGame> getIncompleteGameLive();

    @Query("SELECT * FROM saved_games "
            + "WHERE isDailyChallenge = 1 AND startedAt >= :startOfDay AND startedAt < :endOfDay "
            + "ORDER BY isCompleted ASC, lastPlayedAt DESC LIMIT 1")
    SavedGame getDailyGameForDay(long startOfDay, long endOfDay);

    @Query("DELETE FROM saved_games "
            + "WHERE isCompleted = 0 AND isDailyChallenge = 1 "
            + "AND startedAt >= :startOfDay AND startedAt < :endOfDay "
            + "AND gameId != :keepGameId")
    void deleteIncompleteDailyGamesForDayExcept(long startOfDay, long endOfDay, String keepGameId);

    @Query("DELETE FROM saved_games "
            + "WHERE isCompleted = 0 AND isDailyChallenge = 0 "
            + "AND levelNumber = :levelNumber AND gameId != :keepGameId")
    void deleteIncompleteGamesForLevelExcept(int levelNumber, String keepGameId);
    
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
    
    @Query("SELECT COUNT(*) as count FROM game_history WHERE playedAt >= :start AND playedAt < :end")
    int getGameCountInRange(long start, long end);

    @Query("SELECT AVG(totalTimeMs) FROM game_history WHERE playedAt >= :start AND playedAt < :end AND totalTimeMs > 0")
    double getAverageTimeInRange(long start, long end);

    @Query("SELECT * FROM game_history WHERE playedAt >= :since ORDER BY playedAt DESC")
    List<GameHistory> getGamesSince(long since);
    
    @Query("SELECT COUNT(*) FROM game_history WHERE playedAt >= :since")
    int getGamesCountSince(long since);

    @Query("SELECT COUNT(*) FROM game_history WHERE playedAt >= :since AND mistakesMade < :maxMistakes")
    int getLowMistakeGamesCountSince(long since, int maxMistakes);

    @Query("SELECT COUNT(*) FROM game_history WHERE playedAt >= :since AND levelNumber > :minLevel AND levelNumber <= :maxLevel")
    int getMediumGamesCountSince(long since, int minLevel, int maxLevel);

    @Query("SELECT COUNT(*) FROM game_history WHERE isDailyChallenge = 1")
    int getTotalDailyChallengesCompleted();
    
    @Query("SELECT AVG(totalTimeMs) FROM game_history WHERE playedAt >= :since")
    double getAverageCompletionTime(long since);

    @Query("SELECT AVG(totalTimeMs) FROM game_history WHERE playedAt >= :start AND playedAt < :end AND totalTimeMs > 0")
    double getAverageCompletionTimeForWindow(long start, long end);
    
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
