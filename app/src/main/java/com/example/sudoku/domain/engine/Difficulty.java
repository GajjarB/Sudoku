package com.example.sudoku.domain.engine;

/**
 * Puzzle difficulty. Clue count is a coarse heuristic only; the
 * authoritative rating is technique-based via DifficultyRater.
 */
public enum Difficulty {
    BEGINNER(45, 50, 1.0),
    EASY(38, 44, 2.0),
    MEDIUM(32, 37, 3.5),
    HARD(28, 31, 5.0),
    EXPERT(24, 27, 6.5),
    MASTER(22, 23, 8.0),
    EXTREME(SudokuConstants.MIN_UNIQUE_CLUES, 21, 10.0);

    private final int minClues;
    private final int maxClues;
    private final double maxStrategyScore;

    Difficulty(int minClues, int maxClues, double maxStrategyScore) {
        this.minClues = minClues;
        this.maxClues = maxClues;
        this.maxStrategyScore = maxStrategyScore;
    }

    public int minClues() { return minClues; }
    public int maxClues() { return maxClues; }
    public double maxStrategyScore() { return maxStrategyScore; }

    /** Midpoint clue target — caller refines via rater. */
    public int targetClues() {
        return (minClues + maxClues) / 2;
    }

    public static Difficulty fromClueCount(int clues) {
        for (Difficulty d : values()) {
            if (clues >= d.minClues && clues <= d.maxClues) return d;
        }
        return clues > EASY.maxClues ? BEGINNER : EXTREME;
    }

    public static Difficulty fromStrategyScore(double score) {
        Difficulty best = BEGINNER;
        for (Difficulty d : values()) {
            if (score <= d.maxStrategyScore) return d;
            best = d;
        }
        return best;
    }
}
