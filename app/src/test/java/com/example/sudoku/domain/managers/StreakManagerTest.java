package com.example.sudoku.domain.managers;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.junit.Assert.*;

/**
 * Tests for StreakManager: consecutive day detection, reset logic, timezone safety.
 */
public class StreakManagerTest {

    private StreakManager streakManager;

    @Before
    public void setUp() {
        streakManager = new StreakManager();
    }

    // ═══════════════════════════════════════════════════════════════
    // specificDateExtendsStreak
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void extendsStreak_consecutiveDays_returnsTrue() {
        long yesterday = toEpochMillis(LocalDate.now().minusDays(1));
        long today = toEpochMillis(LocalDate.now());
        assertTrue(streakManager.specificDateExtendsStreak(yesterday, today));
    }

    @Test
    public void extendsStreak_sameDay_returnsFalse() {
        long now = System.currentTimeMillis();
        assertFalse(streakManager.specificDateExtendsStreak(now, now));
    }

    @Test
    public void extendsStreak_twoDaysApart_returnsFalse() {
        long twoDaysAgo = toEpochMillis(LocalDate.now().minusDays(2));
        long today = toEpochMillis(LocalDate.now());
        assertFalse(streakManager.specificDateExtendsStreak(twoDaysAgo, today));
    }

    @Test
    public void extendsStreak_zeroLastPlayed_returnsFalse() {
        assertFalse(streakManager.specificDateExtendsStreak(0, System.currentTimeMillis()));
    }

    @Test
    public void extendsStreak_playedLateNight_thenEarlyMorning_extends() {
        // Yesterday 11:59 PM → Today 12:01 AM = consecutive days
        LocalDate yesterday = LocalDate.now().minusDays(1);
        long lateYesterday = yesterday.atTime(23, 59)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDate today = LocalDate.now();
        long earlyToday = today.atTime(0, 1)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        assertTrue(streakManager.specificDateExtendsStreak(lateYesterday, earlyToday));
    }

    // ═══════════════════════════════════════════════════════════════
    // shouldResetStreak
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void shouldReset_missedOneDay_returnsTrue() {
        long twoDaysAgo = toEpochMillis(LocalDate.now().minusDays(2));
        long today = toEpochMillis(LocalDate.now());
        assertTrue(streakManager.shouldResetStreak(twoDaysAgo, today));
    }

    @Test
    public void shouldReset_consecutiveDays_returnsFalse() {
        long yesterday = toEpochMillis(LocalDate.now().minusDays(1));
        long today = toEpochMillis(LocalDate.now());
        assertFalse(streakManager.shouldResetStreak(yesterday, today));
    }

    @Test
    public void shouldReset_sameDay_returnsFalse() {
        long now = System.currentTimeMillis();
        assertFalse(streakManager.shouldResetStreak(now, now));
    }

    @Test
    public void shouldReset_zeroLastPlayed_returnsTrue() {
        assertTrue(streakManager.shouldResetStreak(0, System.currentTimeMillis()));
    }

    @Test
    public void shouldReset_weekAgo_returnsTrue() {
        long weekAgo = toEpochMillis(LocalDate.now().minusDays(7));
        long today = toEpochMillis(LocalDate.now());
        assertTrue(streakManager.shouldResetStreak(weekAgo, today));
    }

    // ═══════════════════════════════════════════════════════════════
    // isSameDay
    // ═══════════════════════════════════════════════════════════════

    @Test
    public void isSameDay_sameMoment_returnsTrue() {
        long now = System.currentTimeMillis();
        assertTrue(streakManager.isSameDay(now, now));
    }

    @Test
    public void isSameDay_morningAndEvening_returnsTrue() {
        LocalDate today = LocalDate.now();
        long morning = today.atTime(8, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long evening = today.atTime(22, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(streakManager.isSameDay(morning, evening));
    }

    @Test
    public void isSameDay_differentDays_returnsFalse() {
        long yesterday = toEpochMillis(LocalDate.now().minusDays(1));
        long today = toEpochMillis(LocalDate.now());
        assertFalse(streakManager.isSameDay(yesterday, today));
    }

    @Test
    public void isSameDay_zeroLastPlayed_returnsFalse() {
        assertFalse(streakManager.isSameDay(0, System.currentTimeMillis()));
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private long toEpochMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
