package com.example.sudoku.domain.coach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HintResult {
    public final boolean available;
    public final boolean applied;
    public final int hintLevel;
    public final String strategyName;
    public final int targetRow;
    public final int targetCol;
    public final int value;
    public final List<CellPosition> highlightCells;
    public final String shortText;
    public final String longText;

    private HintResult(boolean available, boolean applied, int hintLevel, String strategyName,
                       int targetRow, int targetCol, int value, List<CellPosition> highlightCells,
                       String shortText, String longText) {
        this.available = available;
        this.applied = applied;
        this.hintLevel = hintLevel;
        this.strategyName = strategyName;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.value = value;
        this.highlightCells = Collections.unmodifiableList(new ArrayList<>(highlightCells));
        this.shortText = shortText;
        this.longText = longText;
    }

    public static HintResult unavailable(String message) {
        return new HintResult(false, false, 0, "", -1, -1, 0,
                Collections.emptyList(), message, message);
    }

    public static HintResult fromStrategy(StrategyResult result, int hintLevel) {
        boolean finalAnswer = result.placement && hintLevel >= 4;
        String shortText;
        String longText;
        switch (hintLevel) {
            case 1:
                shortText = "Look at the highlighted area.";
                longText = result.placement
                        ? "Focus on the highlighted cells. One move is available there."
                        : "Focus on the highlighted cells. A candidate can be eliminated there.";
                break;
            case 2:
                shortText = result.explanationShort;
                longText = result.explanationLong;
                break;
            case 3:
                shortText = "Strategy: " + displayName(result.strategyName);
                longText = result.explanationLong;
                break;
            default:
                if (result.placement) {
                    shortText = "Place " + result.value + " at row " + (result.targetRow + 1)
                            + ", column " + (result.targetCol + 1) + ".";
                } else {
                    shortText = "Remove " + result.value + " from row " + (result.targetRow + 1)
                            + ", column " + (result.targetCol + 1) + ".";
                }
                longText = shortText;
                break;
        }
        return new HintResult(true, finalAnswer, hintLevel, result.strategyName,
                result.targetRow, result.targetCol, result.value, result.highlightCells,
                shortText, longText);
    }

    private static String displayName(String strategyName) {
        if ("NAKED_SINGLE".equals(strategyName)) return "Naked Single";
        if ("HIDDEN_SINGLE".equals(strategyName)) return "Hidden Single";
        return strategyName.replace('_', ' ');
    }
}
