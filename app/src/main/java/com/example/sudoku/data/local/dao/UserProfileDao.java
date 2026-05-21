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

    @Query("UPDATE user_profile SET graceUsesThisMonth = :uses, graceResetMonth = :month WHERE userId = :userId")
    void updateGraceFields(String userId, int uses, String month);

    @Query("UPDATE user_profile SET "
            + "totalGamesPlayed = totalGamesPlayed + 1, "
            + "totalGamesWon = totalGamesWon + :wins, "
            + "totalGamesLost = totalGamesLost + :losses, "
            + "totalStarsEarned = totalStarsEarned + :stars, "
            + "totalHintsUsed = totalHintsUsed + :hintsUsed, "
            + "totalPlayTimeMs = totalPlayTimeMs + :timeMs, "
            + "currentXP = currentXP + :xp, "
            + "totalXP = totalXP + :xp, "
            + "currentLevel = CASE WHEN currentLevel < :level THEN :level ELSE currentLevel END, "
            + "lastPlayedDate = :playedAt, "
            + "updatedAt = :playedAt "
            + "WHERE userId = :userId")
    void recordGameResult(String userId, int wins, int losses, int stars,
                          int hintsUsed, long timeMs, int xp, int level, long playedAt);
}
