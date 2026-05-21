package com.example.sudoku.domain.coach;

public class HintEngine {
    private final StrategyDetector strategyDetector;

    public HintEngine() {
        this(new StrategyDetector());
    }

    public HintEngine(StrategyDetector strategyDetector) {
        this.strategyDetector = strategyDetector;
    }

    public HintResult getHint(int[][] grid, int[][] solution, int hintLevel) {
        StrategyResult result = strategyDetector.detectNextMove(grid, solution);
        if (!result.found) {
            return HintResult.unavailable("No logic hint is available for this board yet.");
        }
        int safeLevel = Math.max(1, Math.min(4, hintLevel));
        return HintResult.fromStrategy(result, safeLevel);
    }
}
