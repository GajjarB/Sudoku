package com.example.sudoku.domain.coach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of an elimination strategy. Each entry tells the solver to clear
 * a specific candidate bit from a specific cell.
 */
public final class CandidateElimination {

    public final int row;
    public final int col;
    public final int value;

    public CandidateElimination(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    /** Strategy hit reported by an elimination detector. */
    public static final class StrategyHit {
        public final boolean found;
        public final String strategyName;
        public final List<CandidateElimination> eliminations;
        public final List<CellPosition> highlightCells;
        public final String explanation;

        private StrategyHit(boolean found, String name,
                            List<CandidateElimination> elims,
                            List<CellPosition> highlights,
                            String explanation) {
            this.found = found;
            this.strategyName = name;
            this.eliminations = Collections.unmodifiableList(new ArrayList<>(elims));
            this.highlightCells = Collections.unmodifiableList(new ArrayList<>(highlights));
            this.explanation = explanation;
        }

        public static StrategyHit none() {
            return new StrategyHit(false, "", Collections.emptyList(),
                    Collections.emptyList(), "");
        }

        public static StrategyHit hit(String name,
                                      List<CandidateElimination> elims,
                                      List<CellPosition> highlights,
                                      String explanation) {
            return new StrategyHit(true, name, elims, highlights, explanation);
        }
    }
}
