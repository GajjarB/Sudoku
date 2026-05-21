package com.example.sudoku.domain.ai;

import android.util.Log;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.*;
import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * BEHAVIORAL ANALYZER — Real-Time Learning
 * 
 * This class analyzes player behavior patterns in real-time.
 * Every cell interaction is tracked and analyzed to understand HOW
 * the player thinks, not just what they do.
 */
public class BehavioralAnalyzer {
    
    private static final String TAG = "BehavioralAnalyzer";
    
    private final GameDao dao;
    private final ExecutorService executor;

    // Track cell interactions in current session (in-memory cache)
    private final Map<String, CellInteraction> currentSessionTracking;

    public BehavioralAnalyzer(GameDao dao) {
        this(dao, AppExecutors.getInstance().io());
    }

    public BehavioralAnalyzer(GameDao dao, ExecutorService executor) {
        this.dao = dao;
        this.executor = executor;
        this.currentSessionTracking = new HashMap<>();
    }
    
    /**
     * RECORD CELL INTERACTION
     * 
     * Called every time the player places a number in a cell.
     * This captures HOW the player thinks, not just what they do.
     */
    public void recordCellInteraction(String sessionId, int row, int col, 
                                     int number, boolean correct, 
                                     long thinkingTime, SavedGame game) {
        
        String cellKey = row + "," + col;
        CellInteraction interaction = currentSessionTracking.get(cellKey);
        
        if (interaction == null) {
            interaction = new CellInteraction(row, col);
            currentSessionTracking.put(cellKey, interaction);
        }
        
        interaction.recordAttempt(number, correct, thinkingTime);
        
        // Detect which strategy was likely used
        String strategy = detectStrategy(game, row, col, number);
        
        // Create behavior record
        PlayerBehavior behavior = new PlayerBehavior();
        behavior.sessionId = sessionId;
        behavior.levelNumber = game.levelNumber;
        behavior.timestamp = System.currentTimeMillis();
        behavior.row = row;
        behavior.col = col;
        behavior.numberPlaced = number;
        behavior.wasCorrect = correct;
        behavior.thinkingTimeMs = thinkingTime;
        behavior.changeCount = interaction.changeCount;
        behavior.strategyUsed = strategy;
        behavior.emptyCellsRemaining = countEmptyCells(game.currentGrid);
        behavior.gridComplexity = calculateGridComplexity(game.currentGrid);
        
        // Save to database asynchronously
        executor.execute(() -> {
            try {
                dao.insertPlayerBehavior(behavior);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save player behavior", e);
            }
        });
        
        // Update pattern learning
        updatePatternKnowledge(strategy, correct, thinkingTime);
        
        Log.d(TAG, String.format("Cell (%d,%d): %d | Strategy: %s | Time: %dms | Correct: %b",
                row, col, number, strategy, thinkingTime, correct));
    }
    
    /**
     * DETECT STRATEGY USED
     * 
     * Analyzes the current grid state to determine which Sudoku-solving
     * strategy the player likely used to place this number.
     */
    private String detectStrategy(SavedGame game, int row, int col, int num) {
        int[][] grid = stringToGrid(game.currentGrid);
        
        // Get all possible candidates for this cell
        List<Integer> candidates = GameValidator.getCandidates(grid, row, col);
        
        // NAKED SINGLE: Only one possible number for this cell
        if (candidates.size() == 1 && candidates.contains(num)) {
            return Constants.STRATEGY_NAKED_SINGLE; // Easiest strategy
        }
        
        // HIDDEN SINGLE: Only place in row/col/box for this number
        if (isHiddenSingle(grid, row, col, num)) {
            return Constants.STRATEGY_HIDDEN_SINGLE; // Medium difficulty
        }
        
        // NAKED PAIR: Part of naked pair/triple analysis
        if (isPartOfNakedPair(grid, row, col, num)) {
            return Constants.STRATEGY_NAKED_PAIR; // Advanced
        }
        
        // BOX-LINE REDUCTION: Pointing pairs or box-line reduction
        if (isBoxLineReduction(grid, row, col, num)) {
            return Constants.STRATEGY_BOX_LINE_REDUCTION; // Advanced
        }
        
        // GUESSING: Too many candidates (likely random attempt)
        if (candidates.size() > 3) {
            return Constants.STRATEGY_GUESSING; // Red flag
        }
        
        // Default: General logical deduction
        return Constants.STRATEGY_LOGICAL_DEDUCTION;
    }
    
    private boolean isHiddenSingle(int[][] grid, int row, int col, int num) {
        // Check row
        int possibleInRow = 0;
        for (int c = 0; c < 9; c++) {
            if (grid[row][c] == 0 && GameValidator.getCandidates(grid, row, c).contains(num)) {
                possibleInRow++;
            }
        }
        if (possibleInRow == 1) return true;
        
        // Check column
        int possibleInCol = 0;
        for (int r = 0; r < 9; r++) {
            if (grid[r][col] == 0 && GameValidator.getCandidates(grid, r, col).contains(num)) {
                possibleInCol++;
            }
        }
        if (possibleInCol == 1) return true;
        
        // Check 3x3 box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        int possibleInBox = 0;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == 0 && GameValidator.getCandidates(grid, r, c).contains(num)) {
                    possibleInBox++;
                }
            }
        }
        return possibleInBox == 1;
    }
    
    private boolean isPartOfNakedPair(int[][] grid, int row, int col, int num) {
        List<Integer> candidates = GameValidator.getCandidates(grid, row, col);
        return candidates.size() == 2 && candidates.contains(num);
    }
    
    private boolean isBoxLineReduction(int[][] grid, int row, int col, int num) {
        // Box-line reduction: if a number can only appear in one row (or col)
        // within a box, it's a box-line reduction.
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;

        // Check if num is confined to one row within its box
        Set<Integer> rowsWithNum = new HashSet<>();
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == 0 && GameValidator.getCandidates(grid, r, c).contains(num)) {
                    rowsWithNum.add(r);
                }
            }
        }
        if (rowsWithNum.size() == 1 && rowsWithNum.contains(row)) {
            return true;
        }

        // Check if num is confined to one column within its box
        Set<Integer> colsWithNum = new HashSet<>();
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == 0 && GameValidator.getCandidates(grid, r, c).contains(num)) {
                    colsWithNum.add(c);
                }
            }
        }
        return colsWithNum.size() == 1 && colsWithNum.contains(col);
    }
    
    /**
     * UPDATE PATTERN KNOWLEDGE
     * 
     * Updates the player's proficiency with each strategy.
     */
    private void updatePatternKnowledge(String strategy, boolean success, long timeMs) {
        executor.execute(() -> {
            try {
                SolvingPattern pattern = dao.getSolvingPattern(strategy);

                if (pattern == null) {
                    pattern = new SolvingPattern(strategy);
                }

                if (success) {
                    pattern.successCount++;
                    pattern.consecutiveSuccess++;
                    pattern.consecutiveFailure = 0;
                } else {
                    pattern.failureCount++;
                    pattern.consecutiveFailure++;
                    pattern.consecutiveSuccess = 0;
                }

                double newTime = timeMs / 1000.0;
                if (pattern.avgTimeSeconds == 0) {
                    pattern.avgTimeSeconds = newTime;
                } else {
                    pattern.avgTimeSeconds = pattern.avgTimeSeconds * 0.8 + newTime * 0.2;
                }

                int total = pattern.successCount + pattern.failureCount;
                double rawConfidence = total > 0 ?
                    (double) pattern.successCount / total : 0.5;

                double speedFactor = Math.max(0.5, 1.0 - (pattern.avgTimeSeconds / 60.0));
                pattern.confidenceScore = rawConfidence * 0.7 + speedFactor * 0.3;

                pattern.learningRate = calculateLearningRate(strategy);

                pattern.lastUsed = System.currentTimeMillis();

                dao.upsertSolvingPattern(pattern);

                Log.d(TAG, String.format("Strategy %s: Confidence=%.2f, AvgTime=%.1fs, LearningRate=%.2f",
                        strategy, pattern.confidenceScore, pattern.avgTimeSeconds, pattern.learningRate));
            } catch (Exception e) {
                Log.e(TAG, "Failed to update pattern knowledge for " + strategy, e);
            }
        });
    }
    
    private double calculateLearningRate(String strategy) {
        List<PlayerBehavior> recent = dao.getRecentBehaviorsByStrategy(strategy, 20);
        
        if (recent.size() < 10) return 0;
        
        int mid = recent.size() / 2;
        List<PlayerBehavior> older = recent.subList(mid, recent.size());
        List<PlayerBehavior> newer = recent.subList(0, mid);
        
        double oldAccuracy = calculateAccuracy(older);
        double newAccuracy = calculateAccuracy(newer);
        double oldSpeed = calculateAvgTime(older);
        double newSpeed = calculateAvgTime(newer);
        
        double accuracyDelta = newAccuracy - oldAccuracy;
        double speedDelta = (oldSpeed - newSpeed) / Math.max(oldSpeed, 1.0);
        
        return accuracyDelta * 0.6 + speedDelta * 0.4;
    }
    
    /**
     * ANALYZE COGNITIVE PATTERNS
     * 
     * Called once per day to compute comprehensive cognitive metrics.
     */
    public CognitiveMetrics analyzeCognitivePatterns() {
        long weekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        List<PlayerBehavior> weekData = dao.getBehaviorSince(weekAgo);
        
        if (weekData.isEmpty()) {
            return createDefaultMetrics();
        }
        
        CognitiveMetrics metrics = new CognitiveMetrics(getCurrentDateKey());
        
        List<Long> times = weekData.stream()
            .map(b -> b.thinkingTimeMs)
            .collect(Collectors.toList());
        
        metrics.avgDecisionTimeMs = calculateMean(times);
        metrics.fastestDecisionMs = Collections.min(times);
        metrics.slowestDecisionMs = Collections.max(times);
        
        long correct = weekData.stream().filter(b -> b.wasCorrect).count();
        metrics.errorRate = 1.0 - ((double) correct / weekData.size());
        
        long corrections = weekData.stream()
            .filter(b -> b.changeCount > 1)
            .count();
        metrics.correctionRate = (double) corrections / weekData.size();
        
        metrics.hesitationScore = calculateHesitationScore(weekData);
        metrics.focusScore = calculateFocusScore(times);
        metrics.fatigueIndex = calculateFatigueIndex(weekData);
        
        metrics.improvementRate = calculateImprovementRate(weekData);
        metrics.plateauScore = detectPlateau(weekData);
        metrics.challengeReadiness = calculateChallengeReadiness(metrics);
        
        executor.execute(() -> {
            try {
                dao.insertCognitiveMetrics(metrics);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save cognitive metrics", e);
            }
        });
        
        Log.d(TAG, String.format("Cognitive Analysis: ReadyScore=%d, PlateauScore=%.2f, ErrorRate=%.2f",
                metrics.challengeReadiness, metrics.plateauScore, metrics.errorRate));
        
        return metrics;
    }
    
    private double detectPlateau(List<PlayerBehavior> data) {
        if (data.size() < 30) return 0;
        
        List<PlayerBehavior> recent = data.subList(0, Math.min(30, data.size()));
        
        List<Double> performances = recent.stream()
            .map(b -> b.wasCorrect ? 1.0 : 0.0)
            .collect(Collectors.toList());
        
        double variance = calculateVariance(performances);
        double avgPerformance = performances.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.5);
        
        if (variance < 0.1 && avgPerformance > 0.8) {
            return 0.9;
        } else if (variance < 0.2 && avgPerformance > 0.7) {
            return 0.6;
        } else if (variance < 0.1 && avgPerformance < 0.5) {
            return 0.3;
        }
        
        return 0.2;
    }
    
    private int calculateChallengeReadiness(CognitiveMetrics metrics) {
        int readiness = 0;
        
        if (metrics.errorRate < 0.1) readiness += 30;
        else if (metrics.errorRate < 0.2) readiness += 20;
        else if (metrics.errorRate < 0.3) readiness += 10;
        
        if (metrics.avgDecisionTimeMs < 3000) readiness += 25;
        else if (metrics.avgDecisionTimeMs < 5000) readiness += 15;
        else if (metrics.avgDecisionTimeMs < 8000) readiness += 8;
        
        if (metrics.focusScore > 0.8) readiness += 20;
        else if (metrics.focusScore > 0.6) readiness += 12;
        
        if (metrics.hesitationScore < 0.3) readiness += 15;
        else if (metrics.hesitationScore < 0.5) readiness += 8;
        
        if (metrics.improvementRate > 0.1) readiness += 10;
        else if (metrics.improvementRate > 0) readiness += 5;
        
        readiness -= (int) (metrics.plateauScore * 20);
        
        return Math.max(0, Math.min(100, readiness));
    }
    
    private double calculateHesitationScore(List<PlayerBehavior> data) {
        List<Long> times = data.stream()
            .map(b -> b.thinkingTimeMs)
            .collect(Collectors.toList());
        
        double variance = calculateVariance(times.stream()
            .map(Long::doubleValue)
            .collect(Collectors.toList()));
        
        double mean = calculateMean(times);
        return mean > 0 ? Math.sqrt(variance) / mean : 0;
    }
    
    private double calculateFocusScore(List<Long> times) {
        double variance = calculateVariance(times.stream()
            .map(Long::doubleValue)
            .collect(Collectors.toList()));
        return 1.0 / (1.0 + variance / 10000.0);
    }
    
    private double calculateFatigueIndex(List<PlayerBehavior> data) {
        Map<String, List<PlayerBehavior>> bySessions = data.stream()
            .collect(Collectors.groupingBy(b -> b.sessionId));
        
        double totalFatigue = 0;
        int sessionCount = 0;
        
        for (List<PlayerBehavior> session : bySessions.values()) {
            if (session.size() < 10) continue;
            
            int mid = session.size() / 2;
            List<PlayerBehavior> firstHalf = session.subList(0, mid);
            List<PlayerBehavior> secondHalf = session.subList(mid, session.size());
            
            double firstAccuracy = calculateAccuracy(firstHalf);
            double secondAccuracy = calculateAccuracy(secondHalf);
            
            totalFatigue += Math.max(0, firstAccuracy - secondAccuracy);
            sessionCount++;
        }
        
        return sessionCount > 0 ? totalFatigue / sessionCount : 0;
    }
    
    private double calculateImprovementRate(List<PlayerBehavior> data) {
        if (data.size() < 20) return 0;
        
        int split = data.size() / 2;
        List<PlayerBehavior> recent = data.subList(0, split);
        List<PlayerBehavior> older = data.subList(split, data.size());
        
        double recentAcc = calculateAccuracy(recent);
        double olderAcc = calculateAccuracy(older);
        double recentSpeed = calculateAvgTime(recent);
        double olderSpeed = calculateAvgTime(older);
        
        double accImprovement = recentAcc - olderAcc;
        double speedImprovement = (olderSpeed - recentSpeed) / Math.max(olderSpeed, 1.0);
        
        return accImprovement * 0.6 + speedImprovement * 0.4;
    }
    
    private double calculateAccuracy(List<PlayerBehavior> data) {
        if (data.isEmpty()) return 0;
        long correct = data.stream().filter(b -> b.wasCorrect).count();
        return (double) correct / data.size();
    }
    
    private double calculateAvgTime(List<PlayerBehavior> data) {
        if (data.isEmpty()) return 0;
        return data.stream()
            .mapToLong(b -> b.thinkingTimeMs)
            .average()
            .orElse(0);
    }
    
    private double calculateMean(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }
    
    private double calculateVariance(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
    }
    
    private int countEmptyCells(String grid) {
        return (int) grid.chars().filter(c -> c == '0').count();
    }
    
    private double calculateGridComplexity(String grid) {
        return countEmptyCells(grid) / 81.0;
    }
    
    private int[][] stringToGrid(String s) {
        int[][] grid = new int[9][9];
        for (int i = 0; i < 81; i++) {
            grid[i / 9][i % 9] = s.charAt(i) - '0';
        }
        return grid;
    }
    
    private String getCurrentDateKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
    
    private CognitiveMetrics createDefaultMetrics() {
        return new CognitiveMetrics(getCurrentDateKey());
    }
    
    public void clearSession() {
        currentSessionTracking.clear();
    }

    /**
     * Release per-session state. Shared executor lifecycle is owned by AppExecutors.
     */
    public void shutdown() {
        clearSession();
    }

    public void close() {
        shutdown();
    }
    
    private static class CellInteraction {
        int row, col;
        int changeCount = 0;
        List<Long> attemptTimes = new ArrayList<>();
        
        CellInteraction(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        void recordAttempt(int num, boolean correct, long time) {
            changeCount++;
            attemptTimes.add(time);
        }
    }
}
