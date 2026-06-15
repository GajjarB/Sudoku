package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.dao.UserProfileDao;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.domain.managers.StreakManager;
import com.example.sudoku.utils.Constants;
import java.util.concurrent.ExecutorService;

/**
 * Repository for managing user profile and settings.
 */
public class UserRepository {

    private final UserProfileDao userProfileDao;
    private final ExecutorService executorService;
    private final StreakManager streakManager;
    private LiveData<UserProfile> userProfileLive;

    public UserRepository(UserProfileDao userProfileDao) {
        this(userProfileDao, AppExecutors.getInstance().io());
    }

    public UserRepository(UserProfileDao userProfileDao, ExecutorService executorService) {
        this.userProfileDao = userProfileDao;
        this.executorService = executorService;
        this.streakManager = new StreakManager();
        this.userProfileLive = userProfileDao.getUserProfileLive();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfileLive;
    }

    public void createOrUpdateProfile(UserProfile profile) {
        executorService.execute(() -> userProfileDao.insertUserProfile(profile));
    }

    public void updateXPAndLevel(String userId, int xp, int totalXp, int level) {
        executorService.execute(() -> userProfileDao.updateXPAndLevel(userId, xp, totalXp, level));
    }

    public void awardXPSync(String userId, int xpEarned) {
        UserProfile profile = userProfileDao.getUserProfile();
        if (profile != null) {
            int newXp = profile.currentXP + xpEarned;
            int newTotalXp = profile.totalXP + xpEarned;
            int newLevel = (int) Math.max(1, Math.pow(newTotalXp / 100.0, 1.0 / 1.5));
            userProfileDao.updateXPAndLevel(userId, newXp, newTotalXp, newLevel);
        }
    }

    public void updateStreak(String userId, int currentStreak, int longestStreak) {
        executorService.execute(() -> userProfileDao.updateStreak(userId, currentStreak, longestStreak));
    }

    public void updateEncouragement(String userId, boolean enabled) {
        // Assuming encouragement maps to sound or a new field, but matching DAO:
        // Let's implement the settings updates available in DAO
    }

    public void setSoundEnabled(String userId, boolean enabled) {
        executorService.execute(() -> userProfileDao.updateSoundEnabled(userId, enabled));
    }
    
    public void setVibrationEnabled(String userId, boolean enabled) {
        executorService.execute(() -> userProfileDao.updateVibrationEnabled(userId, enabled));
    }
    
    public void setTheme(String userId, String theme) {
        executorService.execute(() -> userProfileDao.updateTheme(userId, theme));
    }

    public void recordGameResult(String userId, boolean won, int stars, int hintsUsed,
                                 long timeMs, int xp, int level, long playedAt) {
        executorService.execute(() -> recordGameResultSync(
                userId, won, stars, hintsUsed, timeMs, xp, level, playedAt));
    }

    public void recordGameResultSync(String userId, boolean won, int stars, int hintsUsed,
                                     long timeMs, int xp, int level, long playedAt) {
        ensureProfileExistsSync(userId);
        UserProfile profile = userProfileDao.getUserProfile();
        int currentStreak = profile != null ? profile.currentStreak : 0;
        int longestStreak = profile != null ? profile.longestStreak : 0;
        long lastPlayedDate = profile != null ? profile.lastPlayedDate : 0;

        if (lastPlayedDate == 0) {
            if (won) {
                currentStreak = 1;
            }
        } else if (streakManager.isSameDay(lastPlayedDate, playedAt)) {
            // Same calendar day — streak unchanged regardless of win/loss
            currentStreak = Math.max(1, currentStreak);
        } else if (won && streakManager.specificDateExtendsStreak(lastPlayedDate, playedAt)) {
            // Consecutive day AND won — increment streak
            currentStreak += 1;
        } else if (won && streakManager.shouldResetStreak(lastPlayedDate, playedAt)) {
            // Missed at least one day AND won — try grace period
            String currentMonth = getCurrentMonthKey(playedAt);
            int graceUses = profile != null ? profile.graceUsesThisMonth : 0;
            String graceMonth = profile != null ? profile.graceResetMonth : "";
            if (!currentMonth.equals(graceMonth)) {
                graceUses = 0;
            }
            if (graceUses < Constants.GRACE_PERIODS_PER_MONTH && currentStreak > 0) {
                graceUses++;
                userProfileDao.updateGraceFields(userId, graceUses, currentMonth);
                currentStreak = Math.max(1, currentStreak);
            } else {
                currentStreak = 0;
            }
        } else if (won) {
            // First game ever or gap too large with no active streak
            currentStreak = 1;
        }
        // else: loss on a different day — streak stays unchanged
        longestStreak = Math.max(longestStreak, currentStreak);

        userProfileDao.recordGameResult(
                userId,
                won ? 1 : 0,
                won ? 0 : 1,
                won ? stars : 0,
                hintsUsed,
                timeMs,
                won ? xp : 0,
                level,
                playedAt);
        userProfileDao.updateStreak(userId, currentStreak, longestStreak);
    }
    
    /**
     * Initializes a default profile if one doesn't exist.
     * Should be called on app start.
     */
    public void ensureProfileExists(String userId) {
        executorService.execute(() -> {
            try {
                ensureProfileExistsSync(userId);
            } catch (Exception e) {
                android.util.Log.e("UserRepository", "ensureProfileExists failed", e);
            }
        });
    }

    public void ensureProfileExistsSync(String userId) {
        if (userProfileDao.getUserProfile() == null) {
            userProfileDao.insertUserProfile(new UserProfile(userId));
        }
    }

    private String getCurrentMonthKey(long epochMillis) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(epochMillis);
        return cal.get(java.util.Calendar.YEAR) + "-"
                + String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.MONTH) + 1);
    }
}
