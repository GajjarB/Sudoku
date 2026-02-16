package com.example.sudoku.domain.managers;

import java.util.concurrent.TimeUnit;

/**
 * Manages daily streaks logic.
 */
public class StreakManager {

    /**
     * Check if playing today extends the streak.
     * @param lastPlayedTime Time of last completed game
     * @return true if streak should increment, false if it stays same or resets
     */
    public boolean specificDateExtendsStreak(long lastPlayedTime, long currentTime) {
        long oneDayMs = TimeUnit.DAYS.toMillis(1);
        long diff = currentTime - lastPlayedTime;
        
        // If played within last 24-48 hours (roughly), extend
        // Simplified: Check if days are consecutive
        // Real implementation needs Calendar/LocalDate to handle midnight correctly
        // This is a placeholder logic
        
        long daysDiff = diff / oneDayMs;
        return daysDiff == 1;
        
        // Ideally use:
        // LocalDate last = Instant.ofEpochMilli(lastPlayedTime).atZone(ZoneId.systemDefault()).toLocalDate();
        // LocalDate current = Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).toLocalDate();
        // return ChronoUnit.DAYS.between(last, current) == 1;
    }
    
    public boolean shouldResetStreak(long lastPlayedTime, long currentTime) {
        long oneDayMs = TimeUnit.DAYS.toMillis(1);
        return (currentTime - lastPlayedTime) > (oneDayMs * 2); // Missed a day
    }
}
