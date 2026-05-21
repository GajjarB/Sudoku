package com.example.sudoku.domain.managers;

import java.util.Calendar;

/**
 * Manages daily streaks logic.
 * Uses Calendar for API-24-safe local date comparisons.
 */
public class StreakManager {

    /**
     * Check if playing today extends the streak.
     * Uses local calendar days to handle midnight crossings and DST correctly.
     *
     * @param lastPlayedTime Time of last completed game (epoch millis)
     * @param currentTime    Current time (epoch millis)
     * @return true if streak should increment (consecutive calendar day)
     */
    public boolean specificDateExtendsStreak(long lastPlayedTime, long currentTime) {
        if (lastPlayedTime <= 0) return false;

        long daysBetween = localDayNumber(currentTime) - localDayNumber(lastPlayedTime);

        return daysBetween == 1;
    }

    /**
     * Check if streak should reset (missed more than 1 calendar day).
     */
    public boolean shouldResetStreak(long lastPlayedTime, long currentTime) {
        if (lastPlayedTime <= 0) return true;

        long daysBetween = localDayNumber(currentTime) - localDayNumber(lastPlayedTime);

        return daysBetween > 1;
    }

    /**
     * Check if playing today is same calendar day (no streak change).
     */
    public boolean isSameDay(long lastPlayedTime, long currentTime) {
        if (lastPlayedTime <= 0) return false;

        return localDayNumber(lastPlayedTime) == localDayNumber(currentTime);
    }

    private long localDayNumber(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (calendar.get(Calendar.ERA) == java.util.GregorianCalendar.BC) {
            year = 1 - year;
        }
        return daysFromCivil(year, month, day);
    }

    private long daysFromCivil(int year, int month, int day) {
        year -= month <= 2 ? 1 : 0;
        long era = Math.floorDiv(year, 400);
        int yearOfEra = (int) (year - era * 400);
        int adjustedMonth = month + (month > 2 ? -3 : 9);
        int dayOfYear = (153 * adjustedMonth + 2) / 5 + day - 1;
        int dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear;
        return era * 146097L + dayOfEra;
    }
}
