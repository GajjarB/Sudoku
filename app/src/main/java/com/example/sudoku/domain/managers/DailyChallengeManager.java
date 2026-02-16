package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.dao.DailyChallengeDao;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the generation and retrieval of the Daily Challenge.
 */
public class DailyChallengeManager {
    
    private final DailyChallengeDao dailyDao;
    private final GameRepository gameRepository;
    private final ExecutorService executorService;
    
    public DailyChallengeManager(DailyChallengeDao dailyDao, GameRepository gameRepository) {
        this.dailyDao = dailyDao;
        this.gameRepository = gameRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public void getOrCreateDailyChallenge(OnChallengeReadyCallback callback) {
        String todayKey = getTodayKey();
        executorService.execute(() -> {
            DailyChallenge existing = dailyDao.getDailyChallenge(todayKey);
            if (existing != null) {
                callback.onChallengeReady(existing);
            } else {
                // Generate new challenge
                long seed = todayKey.hashCode(); // Consistent seed for everyone on this day (if we want same puzzle)
                SudokuGenerator generator = new SudokuGenerator(seed);
                
                // Daily challenge is usually Medium/Hard
                int clues = Constants.CLUES_MEDIUM_MIN;
                SudokuGenerator.Puzzle puzzle = generator.generate(clues);
                
                DailyChallenge newChallenge = new DailyChallenge(todayKey);
                newChallenge.puzzleGrid = puzzle.puzzle;
                newChallenge.solutionGrid = puzzle.solution;
                newChallenge.clueCount = clues;
                newChallenge.seed = seed;
                newChallenge.createdAt = System.currentTimeMillis();
                newChallenge.expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // +1 day (approx)
                
                dailyDao.insertDailyChallenge(newChallenge);
                callback.onChallengeReady(newChallenge);
            }
        });
    }
    
    public void completeChallenge(DailyChallenge challenge, int score, long timeMs) {
        challenge.isCompleted = true;
        challenge.bestScore = Math.max(challenge.bestScore, score);
        challenge.bestTimeMs = (challenge.bestTimeMs == 0) ? timeMs : Math.min(challenge.bestTimeMs, timeMs);
        
        executorService.execute(() -> dailyDao.updateDailyChallenge(challenge));
    }
    
    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
    
    public interface OnChallengeReadyCallback {
        void onChallengeReady(DailyChallenge challenge);
    }
}
