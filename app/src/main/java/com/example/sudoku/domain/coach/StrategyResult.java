package com.example.sudoku.domain.coach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrategyResult {
    public final boolean found;
    public final String strategyName;
    public final int targetRow;
    public final int targetCol;
    public final int value;
    public final boolean placement;
    public final List<CellPosition> highlightCells;
    public final String explanationShort;
    public final String explanationLong;

    private StrategyResult(boolean found, String strategyName, int targetRow, int targetCol,
                           int value, boolean placement, List<CellPosition> highlightCells,
                           String explanationShort, String explanationLong) {
        this.found = found;
        this.strategyName = strategyName;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.value = value;
        this.placement = placement;
        this.highlightCells = Collections.unmodifiableList(new ArrayList<>(highlightCells));
        this.explanationShort = explanationShort;
        this.explanationLong = explanationLong;
    }

    public static StrategyResult none() {
        return new StrategyResult(false, "", -1, -1, 0, false, Collections.emptyList(), "", "");
    }

    public static StrategyResult found(String strategyName, int targetRow, int targetCol,
                                       int value, List<CellPosition> highlightCells,
                                       String explanationShort, String explanationLong) {
        return new StrategyResult(true, strategyName, targetRow, targetCol, value, true,
                highlightCells, explanationShort, explanationLong);
    }

    public static StrategyResult elimination(String strategyName, int targetRow, int targetCol,
                                             int value, List<CellPosition> highlightCells,
                                             String explanationShort, String explanationLong) {
        return new StrategyResult(true, strategyName, targetRow, targetCol, value, false,
                highlightCells, explanationShort, explanationLong);
    }
}
