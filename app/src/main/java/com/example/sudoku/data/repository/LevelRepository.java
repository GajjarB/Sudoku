package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.data.local.dao.LevelProgressDao;
import com.example.sudoku.data.local.entities.LevelProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing level progression and unlocks.
 */
public class LevelRepository {

    private final LevelProgressDao levelDao;
    private final ExecutorService executorService;

    public LevelRepository(LevelProgressDao levelDao) {
        this.levelDao = levelDao;
        this.executorService = Executors.newSingleThreadExecutor();
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

    public void completeLevel(int level, int stars, int score, long timeMs) {
        long now = System.currentTimeMillis();
        executorService.execute(() -> {
            // First try to mark as completed
            levelDao.completeLevel(level, stars, score, timeMs, now);
            // Then update best performance if replayed
            levelDao.updateBestPerformance(level, stars, score, timeMs);
            
            // Auto-unlock next level
            if (level < 999) {
                levelDao.unlockLevel(level + 1);
            }
        });
    }

    /**
     * Initialize all 999 levels on first run.
     */
    public void initializeLevels() {
        executorService.execute(() -> {
            // Check if levels exist (check level 1)
            if (levelDao.getLevelProgress(1) == null) {
                List<LevelProgress> levels = new ArrayList<>();
                for (int i = 1; i <= 999; i++) {
                    levels.add(new LevelProgress(i));
                }
                levelDao.insertAll(levels);
            }
        });
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
