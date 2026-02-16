package com.example.sudoku.domain.ai;

import android.util.Log;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.CognitiveMetrics;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.domain.engine.SudokuGenerator;

import java.util.UUID;

/**
 * SELF-LEARNING GAME CONTROLLER
 * 
 * Shows how to integrate all the AI components into game flow.
 */
public class SelfLearningGameController {
    
    private static final String TAG = "SelfLearningController";
    
    private GameDao dao;
    private BehavioralAnalyzer analyzer;
    private IntelligentPuzzleGenerator puzzleGen;
    private ProgressiveDifficultySystem difficultySystem;
    
    // Track current cell selection
    private long cellSelectedTime;
    private int selectedRow, selectedCol;
    
    public void init(GameDao dao) {
        this.dao = dao;
        this.analyzer = new BehavioralAnalyzer(dao);
        this.puzzleGen = new IntelligentPuzzleGenerator(dao, analyzer);
        this.difficultySystem = new ProgressiveDifficultySystem(dao, analyzer, puzzleGen);
        
        Log.d(TAG, "Self-learning AI system initialized");
    }
    
    public SavedGame startIntelligentGame(int levelNumber) {
        Log.d(TAG, String.format("Starting intelligent game for level %d", levelNumber));
        
        SudokuGenerator.Puzzle puzzle = difficultySystem.generateNextChallenge(levelNumber);
        
        String gameId = UUID.randomUUID().toString();
        String lockedCells = createLockedCells(puzzle.puzzle);
        
        SavedGame game = new SavedGame(
            gameId, levelNumber, 
            puzzle.puzzle, puzzle.solution, lockedCells
        );
        
        dao.insertSavedGame(game);
        
        Log.d(TAG, String.format("Game created: ID=%s, Clues=%d", gameId, puzzle.clueCount));
        return game;
    }
    
    public void onCellSelected(int row, int col) {
        cellSelectedTime = System.currentTimeMillis();
        selectedRow = row;
        selectedCol = col;
    }
    
    public void onNumberEntered(SavedGame game, int number) {
        long thinkingTime = System.currentTimeMillis() - cellSelectedTime;
        
        int solutionIndex = selectedRow * 9 + selectedCol;
        char solutionChar = game.solutionGrid.charAt(solutionIndex);
        boolean correct = (solutionChar == (char)('0' + number));
        
        Log.d(TAG, String.format("Number entered: %d at (%d,%d), Correct=%b, ThinkTime=%dms",
                number, selectedRow, selectedCol, correct, thinkingTime));
        
        analyzer.recordCellInteraction(
            game.gameId,
            selectedRow, selectedCol,
            number,
            correct,
            thinkingTime,
            game
        );
    }
    
    public void runDailyAnalysis() {
        Log.d(TAG, "=== RUNNING DAILY COGNITIVE ANALYSIS ===");
        
        CognitiveMetrics metrics = analyzer.analyzeCognitivePatterns();
        
        Log.d(TAG, String.format("Challenge Readiness: %d/100", metrics.challengeReadiness));
        Log.d(TAG, String.format("Plateau Score: %.2f", metrics.plateauScore));
        Log.d(TAG, String.format("Improvement Rate: %.2f", metrics.improvementRate));
        Log.d(TAG, String.format("Error Rate: %.2f%%", metrics.errorRate * 100));
        
        Log.d(TAG, "=== DAILY ANALYSIS COMPLETE ===");
    }
    
    private String createLockedCells(String puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 81; i++) {
            sb.append(puzzle.charAt(i) != '0' ? '1' : '0');
        }
        return sb.toString();
    }
    
    public void onGameEnd() {
        analyzer.clearSession();
        Log.d(TAG, "Game session ended, tracking cleared");
    }
}
