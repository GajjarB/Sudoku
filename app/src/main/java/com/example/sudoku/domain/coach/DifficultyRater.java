package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.Difficulty;
import com.example.sudoku.utils.Constants;

import java.util.Map;

/**
 * Rate a puzzle's difficulty by tracking which logical techniques are
 * required to solve it. Higher-difficulty strategies dominate; frequency
 * adds a secondary contribution.
 *
 * Current strategy weights cover only the two implemented detectors. As
 * more detectors land (pairs, x-wing, ...), expand {@link #weightFor}.
 */
public final class DifficultyRater {

    private final LogicalSolver logicalSolver = new LogicalSolver();

    public Rating rate(int[][] puzzle, int[][] solution) {
        LogicalSolver.Result result = logicalSolver.solve(puzzle, solution);
        if (!result.solved) {
            return new Rating(Difficulty.EXTREME, Double.POSITIVE_INFINITY, result.strategyUsage);
        }

        double maxWeight = 0;
        double weightedSum = 0;
        int totalSteps = 0;
        for (Map.Entry<String, Integer> entry : result.strategyUsage.entrySet()) {
            double w = weightFor(entry.getKey());
            int count = entry.getValue();
            maxWeight = Math.max(maxWeight, w);
            weightedSum += w * count;
            totalSteps += count;
        }
        double frequencyFactor = totalSteps > 0 ? weightedSum / totalSteps : 0;
        double score = maxWeight + 0.2 * frequencyFactor;
        return new Rating(Difficulty.fromStrategyScore(score), score, result.strategyUsage);
    }

    private static double weightFor(String strategyName) {
        if (strategyName == null) return 1.0;
        switch (strategyName) {
            case Constants.STRATEGY_NAKED_SINGLE: return 1.0;
            case Constants.STRATEGY_HIDDEN_SINGLE: return 2.0;
            case Constants.STRATEGY_NAKED_PAIR: return 3.5;
            case Constants.STRATEGY_HIDDEN_PAIR: return 4.0;
            case Constants.STRATEGY_POINTING_PAIR: return 4.2;
            case Constants.STRATEGY_BOX_LINE_REDUCTION: return 4.5;
            case Constants.STRATEGY_NAKED_TRIPLE: return 5.0;
            case Constants.STRATEGY_HIDDEN_TRIPLE: return 5.5;
            case Constants.STRATEGY_NAKED_QUAD: return 6.0;
            case Constants.STRATEGY_HIDDEN_QUAD: return 6.5;
            case Constants.STRATEGY_X_WING: return 6.5;
            case Constants.STRATEGY_SWORDFISH: return 7.5;
            case Constants.STRATEGY_JELLYFISH: return 8.0;
            case Constants.STRATEGY_XY_WING: return 7.0;
            case Constants.STRATEGY_UNIQUE_RECTANGLE: return 7.2;
            case Constants.STRATEGY_SIMPLE_COLORING: return 7.5;
            case Constants.STRATEGY_XYZ_WING: return 7.8;
            case Constants.STRATEGY_FORCING_CHAIN: return 8.5;
            case Constants.STRATEGY_LOGICAL_DEDUCTION: return 2.5;
            case Constants.STRATEGY_GUESSING: return 9.0;
            default: return 5.0;
        }
    }

    public static final class Rating {
        public final Difficulty difficulty;
        public final double score;
        public final Map<String, Integer> strategyUsage;

        public Rating(Difficulty difficulty, double score, Map<String, Integer> usage) {
            this.difficulty = difficulty;
            this.score = score;
            this.strategyUsage = usage;
        }
    }
}
