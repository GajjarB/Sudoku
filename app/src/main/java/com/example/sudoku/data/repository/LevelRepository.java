package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.dao.LevelProgressDao;
import com.example.sudoku.data.local.entities.LevelProgress;
import com.example.sudoku.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for managing level progression and unlocks.
 */
public class LevelRepository {

    private final LevelProgressDao levelDao;
    private final ExecutorService executorService;

    private volatile boolean levelsInitialized = false;

    public LevelRepository(LevelProgressDao levelDao) {
        this(levelDao, AppExecutors.getInstance().io());
    }

    public LevelRepository(LevelProgressDao levelDao, ExecutorService executorService) {
        this.levelDao = levelDao;
        this.executorService = executorService;
    }

    public LiveData<List<LevelProgress>> getUnlockedLevels() {
        return levelDao.getUnlockedLevels();
    }

    public LiveData<LevelProgress> getLevelProgress(int levelNumber) {
        return levelDao.getLevelProgressLive(levelNumber);
    }

    public void unlockLevel(int levelNumber) {
        executorService.execute(() -> levelDao.unlockLevel(levelNumber));
    }

    public void unlockLevelSync(int levelNumber) {
        levelDao.unlockLevel(levelNumber);
    }

    public void completeLevelSync(int level, int stars, int score, long timeMs) {
        ensureLevelsInitializedSync();
        long now = System.currentTimeMillis();
        levelDao.completeLevel(level, stars, score, timeMs, now);
        levelDao.updateBestPerformance(level, stars, score, timeMs);
        if (level < Constants.MAX_LEVEL) {
            levelDao.unlockLevel(level + 1);
        }
    }

    public void completeLevel(int level, int stars, int score, long timeMs) {
        long now = System.currentTimeMillis();
        executorService.execute(() -> {
            ensureLevelsInitializedSync();
            // First try to mark as completed
            levelDao.completeLevel(level, stars, score, timeMs, now);
            // Then update best performance if replayed
            levelDao.updateBestPerformance(level, stars, score, timeMs);
            
            // Auto-unlock next level
            if (level < Constants.MAX_LEVEL) {
                levelDao.unlockLevel(level + 1);
            }
        });
    }

    /**
     * Initialize all 999 levels on first run.
     */
    public void initializeLevels() {
        executorService.execute(() -> {
            ensureLevelsInitializedSync();
        });
    }

    public void ensureLevelsInitializedSync() {
        if (levelsInitialized) return;

        if (levelDao.getLevelCount() >= Constants.MAX_LEVEL) {
            levelsInitialized = true;
            return;
        }
        List<LevelProgress> levels = new ArrayList<>();
        for (int i = 1; i <= Constants.MAX_LEVEL; i++) {
            levels.add(new LevelProgress(i));
        }
        levelDao.insertAll(levels);
        levelsInitialized = true;
    }

    void resetInitCacheForTesting() {
        levelsInitialized = false;
    }

    public boolean isLevelUnlockedSync(int levelNumber) {
        ensureLevelsInitializedSync();
        return levelDao.isLevelUnlocked(levelNumber);
    }

    public int getHighestUnlockedLevelSync() {
        ensureLevelsInitializedSync();
        return levelDao.getHighestUnlockedLevel();
    }
    
    public void getTotalStars(DataCallback<Integer> callback) {
        executorService.execute(() -> {
            int stars = levelDao.getTotalStars();
            callback.onResult(stars);
        });
    }
    
    public interface DataCallback<T> {
        void onResult(T result);
    }
}
