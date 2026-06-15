package com.example.sudoku.data.repository;

import androidx.lifecycle.LiveData;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Repository for managing game data (Saved Games, History, AI data).
 * Acts as a single source of truth for game-related data.
 */
public class GameRepository {

    private final GameDao gameDao;
    private final ExecutorService executorService;

    public GameRepository(GameDao gameDao) {
        this(gameDao, AppExecutors.getInstance().io());
    }

    public GameRepository(GameDao gameDao, ExecutorService executorService) {
        this.gameDao = gameDao;
        this.executorService = executorService;
    }

    // ═══════════════════════════════════════════════════════════════
    // SAVED GAMES
    // ═══════════════════════════════════════════════════════════════

    public LiveData<SavedGame> getSavedGameLive(String gameId) {
        return gameDao.getSavedGameLive(gameId);
    }

    public SavedGame getSavedGame(String gameId) {
        return gameDao.getSavedGame(gameId);
    }

    public void saveGame(SavedGame game) {
        executorService.execute(() -> {
            try {
                gameDao.insertSavedGame(game);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "saveGame failed", e);
            }
        });
    }

    public void saveGameSync(SavedGame game) {
        gameDao.insertSavedGame(game);
    }

    public void updateSavedGame(SavedGame game) {
        executorService.execute(() -> {
            try {
                gameDao.updateSavedGame(game);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "updateSavedGame failed", e);
            }
        });
    }

    public void updateSavedGameSync(SavedGame game) {
        gameDao.updateSavedGame(game);
    }

    public void deleteSavedGame(SavedGame game) {
        executorService.execute(() -> {
            try {
                gameDao.deleteSavedGame(game);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "deleteSavedGame failed", e);
            }
        });
    }

    public LiveData<SavedGame> getIncompleteGame() {
        return gameDao.getIncompleteGameLive();
    }

    /** Synchronous lookup — must be called from a background thread. */
    public SavedGame getIncompleteGameSync() {
        return gameDao.getIncompleteGame();
    }

    public SavedGame getDailyGameForDaySync(long startOfDay, long endOfDay) {
        return gameDao.getDailyGameForDay(startOfDay, endOfDay);
    }

    public void deleteIncompleteDailyGamesForDayExceptSync(long startOfDay, long endOfDay,
                                                           String keepGameId) {
        gameDao.deleteIncompleteDailyGamesForDayExcept(startOfDay, endOfDay, keepGameId);
    }

    public void deleteIncompleteGamesForLevelExceptSync(int levelNumber, String keepGameId) {
        gameDao.deleteIncompleteGamesForLevelExcept(levelNumber, keepGameId);
    }

    // ═══════════════════════════════════════════════════════════════
    // GAME HISTORY
    // ═══════════════════════════════════════════════════════════════

    public void recordGameHistory(GameHistory history) {
        executorService.execute(() -> {
            try {
                gameDao.insertGameHistory(history);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "recordGameHistory failed", e);
            }
        });
    }

    public void recordGameHistorySync(GameHistory history) {
        gameDao.insertGameHistory(history);
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
        executorService.execute(() -> {
            try {
                gameDao.insertPlayerBehavior(behavior);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "recordPlayerBehavior failed", e);
            }
        });
    }

    public void saveCognitiveMetrics(CognitiveMetrics metrics) {
        executorService.execute(() -> {
            try {
                gameDao.insertCognitiveMetrics(metrics);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "saveCognitiveMetrics failed", e);
            }
        });
    }

    public void updateSolvingPattern(SolvingPattern pattern) {
        executorService.execute(() -> {
            try {
                gameDao.upsertSolvingPattern(pattern);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "updateSolvingPattern failed", e);
            }
        });
    }
    
    // Cleanup
    public void cleanupOldData() {
        long ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
        executorService.execute(() -> {
            try {
                gameDao.deleteOldCompletedGames(ninetyDaysAgo);
                gameDao.deleteOldBehaviorData(ninetyDaysAgo);
            } catch (Exception e) {
                android.util.Log.e("GameRepository", "Cleanup failed", e);
            }
        });
    }

}
