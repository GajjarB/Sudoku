package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing game data (Saved Games, History, AI data).
 * Acts as a single source of truth for game-related data.
 */
public class GameRepository {

    private final GameDao gameDao;
    private final ExecutorService executorService;

    public GameRepository(GameDao gameDao) {
        this.gameDao = gameDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ═══════════════════════════════════════════════════════════════
    // SAVED GAMES
    // ═══════════════════════════════════════════════════════════════

    public LiveData<SavedGame> getSavedGameLive(String gameId) {
        return gameDao.getSavedGameLive(gameId);
    }

    public void saveGame(SavedGame game) {
        executorService.execute(() -> gameDao.insertSavedGame(game));
    }

    public void updateSavedGame(SavedGame game) {
        executorService.execute(() -> gameDao.updateSavedGame(game));
    }

    public void deleteSavedGame(SavedGame game) {
        executorService.execute(() -> gameDao.deleteSavedGame(game));
    }

    public LiveData<SavedGame> getIncompleteGame() {
        return gameDao.getIncompleteGameLive();
    }

    // ═══════════════════════════════════════════════════════════════
    // GAME HISTORY
    // ═══════════════════════════════════════════════════════════════

    public void recordGameHistory(GameHistory history) {
        executorService.execute(() -> gameDao.insertGameHistory(history));
    }

    public List<GameHistory> getRecentGames(int limit) {
        // Note: This must be called on background thread
        return gameDao.getRecentGames(limit);
    }

    public int getTotalDailyChallengesCompleted() {
        // Note: This must be called on background thread
        return gameDao.getTotalDailyChallengesCompleted();
    }

    // ═══════════════════════════════════════════════════════════════
    // AI & ANALYTICS
    // ═══════════════════════════════════════════════════════════════

    public void recordPlayerBehavior(PlayerBehavior behavior) {
        executorService.execute(() -> gameDao.insertPlayerBehavior(behavior));
    }

    public void saveCognitiveMetrics(CognitiveMetrics metrics) {
        executorService.execute(() -> gameDao.insertCognitiveMetrics(metrics));
    }

    public void updateSolvingPattern(SolvingPattern pattern) {
        executorService.execute(() -> gameDao.upsertSolvingPattern(pattern));
    }
    
    // Cleanup
    public void cleanupOldData() {
        long monthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        executorService.execute(() -> {
            gameDao.deleteOldCompletedGames(monthAgo);
            gameDao.deleteOldBehaviorData(monthAgo);
        });
    }
}
