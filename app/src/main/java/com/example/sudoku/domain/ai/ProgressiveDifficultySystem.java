package com.example.sudoku.domain.ai;

import android.util.Log;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.CognitiveMetrics;
import com.example.sudoku.data.local.entities.SolvingPattern;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.utils.Constants;

import java.util.List;

/**
 * PROGRESSIVE DIFFICULTY SYSTEM — Smart Leveling
 * 
 * Top-level orchestrator. Makes intelligent decisions about next puzzle difficulty.
 */
public class ProgressiveDifficultySystem {
    
    private static final String TAG = "ProgressiveDifficulty";
    
    private GameDao dao;
    private BehavioralAnalyzer analyzer;
    private IntelligentPuzzleGenerator puzzleGen;
    
    public ProgressiveDifficultySystem(GameDao dao, BehavioralAnalyzer analyzer, 
                                      IntelligentPuzzleGenerator puzzleGen) {
        this.dao = dao;
        this.analyzer = analyzer;
        this.puzzleGen = puzzleGen;
    }
    
    /**
     * GENERATE NEXT CHALLENGE
     */
    public SudokuGenerator.Puzzle generateNextChallenge(int levelNumber) {
        CognitiveMetrics metrics = analyzer.analyzeCognitivePatterns();
        
        Log.d(TAG, String.format("=== GENERATING NEXT CHALLENGE (Level %d) ===", levelNumber));
        Log.d(TAG, String.format("Player State: ErrorRate=%.2f, ReadyScore=%d, PlateauScore=%.2f",
                metrics.errorRate, metrics.challengeReadiness, metrics.plateauScore));
        
        if (isPlayerStrugglingSignificantly(metrics)) {
            Log.d(TAG, "→ DECISION: Player is struggling - generating EASIER puzzle");
            return generateEasierPuzzle(levelNumber, "STRUGGLING");
        }
        
        if (isPlayerPlateauing(metrics)) {
            Log.d(TAG, "→ DECISION: Player has plateaued - forcing BREAKTHROUGH");
            return generateBreakthroughPuzzle(levelNumber, metrics);
        }
        
        if (isPlayerMasteringCurrent(metrics)) {
            Log.d(TAG, "→ DECISION: Player is mastering - generating HARDER puzzle");
            return generateHarderPuzzle(levelNumber, metrics);
        }
        
        Log.d(TAG, "→ DECISION: Generating PERSONALIZED puzzle");
        return puzzleGen.generatePersonalizedPuzzle(levelNumber);
    }
    
    private boolean isPlayerStrugglingSignificantly(CognitiveMetrics metrics) {
        boolean struggling = metrics.errorRate > Constants.AI_STRUGGLING_ERROR_RATE ||
                           metrics.challengeReadiness < Constants.AI_STRUGGLING_READINESS ||
                           metrics.improvementRate < -0.2;
        
        if (struggling) {
            Log.d(TAG, "Struggle indicators detected");
        }
        return struggling;
    }
    
    private boolean isPlayerPlateauing(CognitiveMetrics metrics) {
        boolean plateaued = metrics.plateauScore > Constants.AI_PLATEAU_THRESHOLD && 
                          metrics.challengeReadiness > 40;
        
        if (plateaued) {
            Log.d(TAG, "Plateau detected");
        }
        return plateaued;
    }
    
    private boolean isPlayerMasteringCurrent(CognitiveMetrics metrics) {
        boolean mastering = metrics.errorRate < Constants.AI_MASTERING_ERROR_RATE && 
                          metrics.challengeReadiness > Constants.AI_MASTERING_READINESS &&
                          metrics.avgDecisionTimeMs < Constants.AI_MASTERING_DECISION_TIME;
        
        if (mastering) {
            Log.d(TAG, "Mastery detected");
        }
        return mastering;
    }
    
    private SudokuGenerator.Puzzle generateEasierPuzzle(int level, String reason) {
        SudokuGenerator gen = new SudokuGenerator();
        int clues = Constants.CLUES_EASY_MAX;
        return gen.generate(clues);
    }
    
    private SudokuGenerator.Puzzle generateBreakthroughPuzzle(int level, CognitiveMetrics metrics) {
        // Find next strategy to learn
        List<SolvingPattern> patterns = dao.getAllSolvingPatterns();
        String targetStrategy = findNextStrategyToLearn(patterns);
        
        Log.d(TAG, String.format("Target strategy for breakthrough: %s", targetStrategy));
        return puzzleGen.generatePersonalizedPuzzle(level);
    }
    
    private SudokuGenerator.Puzzle generateHarderPuzzle(int level, CognitiveMetrics metrics) {
        SudokuGenerator gen = new SudokuGenerator();
        int clues = Constants.CLUES_EXPERT_MIN + 
                   (int) (Math.random() * (Constants.CLUES_EXPERT_MAX - Constants.CLUES_EXPERT_MIN + 1));
        return gen.generate(clues);
    }
    
    private String findNextStrategyToLearn(List<SolvingPattern> patterns) {
        String[] progression = {
            Constants.STRATEGY_NAKED_SINGLE,
            Constants.STRATEGY_HIDDEN_SINGLE,
            Constants.STRATEGY_NAKED_PAIR,
            Constants.STRATEGY_POINTING_PAIR,
            Constants.STRATEGY_BOX_LINE_REDUCTION,
            Constants.STRATEGY_NAKED_TRIPLE,
            Constants.STRATEGY_HIDDEN_PAIR
        };
        
        for (String strategy : progression) {
            SolvingPattern pattern = patterns.stream()
                .filter(p -> p.patternType.equals(strategy))
                .findFirst()
                .orElse(null);
            
            if (pattern == null || pattern.confidenceScore < 0.6) {
                return strategy;
            }
        }
        
        return Constants.STRATEGY_HIDDEN_PAIR;
    }
}
