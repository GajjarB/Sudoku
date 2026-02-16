package com.example.sudoku.domain.managers;

import android.util.Log;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.domain.models.GameState;
import java.util.UUID;

/**
 * The central brain of the gameplay.
 * Coordinates input, validation, scoring, and saving.
 */
public class GameStateManager {
    
    private static final String TAG = "GameStateManager";
    
    private final GameRepository gameRepository;
    private final ScoreCalculator scoreCalculator;
    
    // In-memory state
    private SavedGame currentGame;
    private GameState currentState = GameState.IDLE;
    private long startTime;
    private long accumulatedTime = 0;
    
    private GameStateListener listener;
    
    public GameStateManager(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.scoreCalculator = new ScoreCalculator();
    }
    
    public void setListener(GameStateListener listener) {
        this.listener = listener;
    }
    
    public void loadGame(SavedGame game) {
        this.currentGame = game;
        this.accumulatedTime = game.elapsedTimeMs;
        this.currentState = GameState.PAUSED; // loaded but waiting to resume
        if (listener != null) listener.onGameLoaded(game);
    }
    
    public void resumeGame() {
        if (currentGame == null) return;
        this.startTime = System.currentTimeMillis();
        this.currentState = GameState.PLAYING;
        if (listener != null) listener.onGameResumed();
    }
    
    public void pauseGame() {
        if (currentState != GameState.PLAYING) return;
        
        // Calculate elapsed
        long now = System.currentTimeMillis();
        long sessionTime = now - startTime;
        accumulatedTime += sessionTime;
        
        // Update game object
        currentGame.elapsedTimeMs = accumulatedTime;
        currentGame.lastPlayedAt = now;
        
        // Save to DB
        gameRepository.updateSavedGame(currentGame);
        
        this.currentState = GameState.PAUSED;
        if (listener != null) listener.onGamePaused();
    }
    
    public void processInput(int row, int col, int number) {
        if (currentState != GameState.PLAYING || currentGame == null) return;
        
        // Logic to update grid string, validate move, etc.
        // This requires parsing the grid string to int[][], updating, and saving back.
        // For performance, we should keep int[][] in memory in this manager.
        // Placeholder for brevity:
        
        // 1. Update in-memory grid
        // 2. Validate
        // 3. Update currentGame fields (mistakes, score)
        // 4. Check for completion
        // 5. Notify listener
        
        Log.d(TAG, "Processed input: " + number + " at " + row + "," + col);
    }
    
    public interface GameStateListener {
        void onGameLoaded(SavedGame game);
        void onGameResumed();
        void onGamePaused();
        void onGameCompleted(int score, int stars);
        void onMistake(int newMistakeCount);
    }
}
