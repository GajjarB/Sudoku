package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.data.local.dao.UserProfileDao;
import com.example.sudoku.data.local.entities.UserProfile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing user profile and settings.
 */
public class UserRepository {

    private final UserProfileDao userProfileDao;
    private final ExecutorService executorService;
    private LiveData<UserProfile> userProfileLive;

    public UserRepository(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
        this.executorService = Executors.newSingleThreadExecutor();
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
    
    /**
     * Initializes a default profile if one doesn't exist.
     * Should be called on app start.
     */
    public void ensureProfileExists(String userId) {
        executorService.execute(() -> {
            if (userProfileDao.getUserProfile() == null) {
                userProfileDao.insertUserProfile(new UserProfile(userId));
            }
        });
    }
}
