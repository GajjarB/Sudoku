package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.dao.DailyChallengeDao;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.utils.Constants;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

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
        this.executorService = AppExecutors.getInstance().io();
    }
    
    public void getOrCreateDailyChallenge(OnChallengeReadyCallback callback) {
        String todayKey = getTodayKey();
        executorService.execute(() -> {
            callback.onChallengeReady(getOrCreateDailyChallengeSync(todayKey));
        });
    }

    public DailyChallenge getOrCreateDailyChallengeSync() {
        return getOrCreateDailyChallengeSync(getTodayKey());
    }

    public DailyChallenge getOrCreateDailyChallengeSync(String dateKey) {
        DailyChallenge existing = dailyDao.getDailyChallenge(dateKey);
        if (existing != null) {
            return existing;
        }

        long seed = dateKey.hashCode();
        SudokuGenerator generator = new SudokuGenerator(seed);
        int clues = Constants.CLUES_MEDIUM_MIN;
        SudokuGenerator.Puzzle puzzle = generator.generate(clues);

        DailyChallenge newChallenge = new DailyChallenge(dateKey);
        newChallenge.puzzleGrid = puzzle.puzzle;
        newChallenge.solutionGrid = puzzle.solution;
        newChallenge.clueCount = clues;
        newChallenge.seed = seed;
        newChallenge.createdAt = System.currentTimeMillis();
        newChallenge.expiresAt = getNextLocalMidnightMillis();

        dailyDao.insertDailyChallenge(newChallenge);
        return newChallenge;
    }
    
    public void completeChallenge(DailyChallenge challenge, int score, long timeMs) {
        challenge.isCompleted = true;
        challenge.bestScore = Math.max(challenge.bestScore, score);
        challenge.bestTimeMs = (challenge.bestTimeMs == 0) ? timeMs : Math.min(challenge.bestTimeMs, timeMs);
        
        executorService.execute(() -> dailyDao.updateDailyChallenge(challenge));
    }

    public void completeChallengeSync(String dateKey, int score, long timeMs) {
        DailyChallenge challenge = getOrCreateDailyChallengeSync(dateKey);
        challenge.isCompleted = true;
        challenge.bestScore = Math.max(challenge.bestScore, score);
        challenge.bestTimeMs = (challenge.bestTimeMs == 0)
                ? timeMs
                : Math.min(challenge.bestTimeMs, timeMs);
        dailyDao.updateDailyChallenge(challenge);
    }
    
    public String getTodayKey() {
        return getDateKey(System.currentTimeMillis());
    }

    public String getDateKey(long epochMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(epochMillis));
    }

    public long getStartOfTodayMillis() {
        return getStartOfLocalDayMillis(System.currentTimeMillis());
    }

    public long getEndOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getStartOfTodayMillis());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private long getNextLocalMidnightMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private long getStartOfLocalDayMillis(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    public interface OnChallengeReadyCallback {
        void onChallengeReady(DailyChallenge challenge);
    }
}
