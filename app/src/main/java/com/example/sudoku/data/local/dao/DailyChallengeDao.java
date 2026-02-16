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
