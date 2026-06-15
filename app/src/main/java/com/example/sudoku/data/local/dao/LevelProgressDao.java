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

    @Query("SELECT COUNT(*) FROM level_progress")
    int getLevelCount();

    @Query("SELECT EXISTS(SELECT 1 FROM level_progress WHERE levelNumber = :level AND isUnlocked = 1)")
    boolean isLevelUnlocked(int level);

    @Query("SELECT COALESCE(MAX(levelNumber), 1) FROM level_progress WHERE isUnlocked = 1")
    int getHighestUnlockedLevel();
    
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

    @Query("UPDATE level_progress SET isUnlocked = CASE WHEN levelNumber = 1 THEN 1 ELSE 0 END, isCompleted = 0, starsEarned = 0, bestScore = 0, bestTimeMs = 9223372036854775807, timesPlayed = 0, timesCompleted = 0, firstCompletedAt = 0, lastPlayedAt = 0")
    void resetLevelProgress();
}
