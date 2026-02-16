package com.example.sudoku.domain.ai;

import android.util.Log;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.*;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.utils.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * INTELLIGENT PUZZLE GENERATOR — Targets Weaknesses
 * 
 * Generates puzzles specifically designed for the individual player.
 * Uses behavioral data to create optimal challenges.
 */
public class IntelligentPuzzleGenerator {
    
    private static final String TAG = "IntelligentPuzzleGen";
    
    private GameDao dao;
    private BehavioralAnalyzer analyzer;
    
    public IntelligentPuzzleGenerator(GameDao dao, BehavioralAnalyzer analyzer) {
        this.dao = dao;
        this.analyzer = analyzer;
    }
    
    /**
     * GENERATE PERSONALIZED PUZZLE
     */
    public SudokuGenerator.Puzzle generatePersonalizedPuzzle(int levelNumber) {
        
        // Step 1: Analyze player's current state
        PlayerProfile profile = analyzePlayerProfile();
        
        // Step 2: Determine optimal difficulty parameters
        PuzzleParameters params = calculateOptimalParameters(profile);
        
        Log.d(TAG, String.format("Generating puzzle: Level=%d, Clues=%d, Readiness=%d",
                levelNumber, params.clueCount, profile.metrics.challengeReadiness));
        
        // Step 3: Generate base puzzle
        SudokuGenerator generator = new SudokuGenerator();
        SudokuGenerator.Puzzle puzzle = generator.generate(params.clueCount);
        
        // Step 4: Customize puzzle to target weak strategies (if needed)
        if (params.forceNewStrategies || !params.weakStrategies.isEmpty()) {
            Log.d(TAG, "Weak strategies detected: " + params.weakStrategies);
        }
        
        return puzzle;
    }
    
    private PlayerProfile analyzePlayerProfile() {
        CognitiveMetrics metrics = dao.getLatestCognitiveMetrics();
        if (metrics == null) {
            metrics = analyzer.analyzeCognitivePatterns();
        }
        
        List<SolvingPattern> patterns = dao.getAllSolvingPatterns();
        List<GameHistory> history = dao.getRecentGames(20);
        
        return new PlayerProfile(metrics, patterns, history);
    }
    
    private PuzzleParameters calculateOptimalParameters(PlayerProfile profile) {
        PuzzleParameters params = new PuzzleParameters();
        int readiness = profile.metrics.challengeReadiness;
        
        if (readiness >= 80) {
            params.clueCount = Constants.CLUES_EXPERT_MIN +
                    (int) (Math.random() * (Constants.CLUES_EXPERT_MAX - Constants.CLUES_EXPERT_MIN + 1));
            params.targetStrategies = Arrays.asList(
                Constants.STRATEGY_HIDDEN_PAIR,
                Constants.STRATEGY_NAKED_TRIPLE,
                Constants.STRATEGY_BOX_LINE_REDUCTION
            );
            Log.d(TAG, "High readiness (80+): Expert difficulty");
            
        } else if (readiness >= 60) {
            params.clueCount = Constants.CLUES_HARD_MIN +
                    (int) (Math.random() * (Constants.CLUES_HARD_MAX - Constants.CLUES_HARD_MIN + 1));
            params.targetStrategies = Arrays.asList(
                Constants.STRATEGY_HIDDEN_SINGLE,
                Constants.STRATEGY_NAKED_PAIR,
                Constants.STRATEGY_POINTING_PAIR
            );
            Log.d(TAG, "Medium-high readiness (60-79): Hard difficulty");
            
        } else if (readiness >= 40) {
            params.clueCount = Constants.CLUES_MEDIUM_MIN +
                    (int) (Math.random() * (Constants.CLUES_MEDIUM_MAX - Constants.CLUES_MEDIUM_MIN + 1));
            params.targetStrategies = Arrays.asList(
                Constants.STRATEGY_NAKED_SINGLE,
                Constants.STRATEGY_HIDDEN_SINGLE
            );
            Log.d(TAG, "Medium readiness (40-59): Medium difficulty");
            
        } else {
            params.clueCount = Constants.CLUES_EASY_MIN +
                    (int) (Math.random() * (Constants.CLUES_EASY_MAX - Constants.CLUES_EASY_MIN + 1));
            params.targetStrategies = Arrays.asList(Constants.STRATEGY_NAKED_SINGLE);
            Log.d(TAG, "Low readiness (<40): Easy difficulty");
        }
        
        if (profile.metrics.plateauScore > Constants.AI_PLATEAU_THRESHOLD) {
            params.clueCount = Math.max(
                Constants.CLUES_EXPERT_MIN,
                params.clueCount - 5
            );
            params.forceNewStrategies = true;
            Log.d(TAG, "PLATEAU DETECTED! Forcing difficulty increase");
        }
        
        params.weakStrategies = identifyWeakStrategies(profile.patterns);
        
        return params;
    }
    
    private List<String> identifyWeakStrategies(List<SolvingPattern> patterns) {
        return patterns.stream()
            .filter(p -> p.confidenceScore < 0.4 || p.learningRate < 0)
            .map(p -> p.patternType)
            .collect(Collectors.toList());
    }
    
    // Helper classes
    private static class PlayerProfile {
        final CognitiveMetrics metrics;
        final List<SolvingPattern> patterns;
        final List<GameHistory> history;
        
        PlayerProfile(CognitiveMetrics metrics, List<SolvingPattern> patterns, List<GameHistory> history) {
            this.metrics = metrics;
            this.patterns = patterns;
            this.history = history;
        }
    }
    
    private static class PuzzleParameters {
        int clueCount;
        List<String> targetStrategies = new ArrayList<>();
        List<String> weakStrategies = new ArrayList<>();
        boolean forceNewStrategies = false;
    }
}
