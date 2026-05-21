package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.coach.strategies.BoxLineReductionDetector;
import com.example.sudoku.domain.coach.strategies.FishDetector;
import com.example.sudoku.domain.coach.strategies.ForcingChainDetector;
import com.example.sudoku.domain.coach.strategies.HiddenPairDetector;
import com.example.sudoku.domain.coach.strategies.HiddenSubsetDetector;
import com.example.sudoku.domain.coach.strategies.NakedPairDetector;
import com.example.sudoku.domain.coach.strategies.NakedSubsetDetector;
import com.example.sudoku.domain.coach.strategies.PointingPairDetector;
import com.example.sudoku.domain.coach.strategies.SimpleColoringDetector;
import com.example.sudoku.domain.coach.strategies.UniqueRectangleDetector;
import com.example.sudoku.domain.coach.strategies.XWingDetector;
import com.example.sudoku.domain.coach.strategies.XYWingDetector;
import com.example.sudoku.domain.coach.strategies.XYZWingDetector;
import com.example.sudoku.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrategyDetector {
    private final List<StrategyRuleDetector> placementDetectors;
    private final List<EliminationDetector> eliminationDetectors;

    public StrategyDetector() {
        this(Arrays.asList(
                new NakedSingleDetector(),
                new HiddenSingleDetector()),
                Arrays.asList(
                        new NakedPairDetector(),
                        new HiddenPairDetector(),
                        new PointingPairDetector(),
                        new BoxLineReductionDetector(),
                        new NakedSubsetDetector(3, Constants.STRATEGY_NAKED_TRIPLE),
                        new HiddenSubsetDetector(3, Constants.STRATEGY_HIDDEN_TRIPLE),
                        new XWingDetector(),
                        new NakedSubsetDetector(4, Constants.STRATEGY_NAKED_QUAD),
                        new HiddenSubsetDetector(4, Constants.STRATEGY_HIDDEN_QUAD),
                        new FishDetector(3, Constants.STRATEGY_SWORDFISH),
                        new XYWingDetector(),
                        new XYZWingDetector(),
                        new UniqueRectangleDetector(),
                        new SimpleColoringDetector(),
                        new FishDetector(4, Constants.STRATEGY_JELLYFISH),
                        new ForcingChainDetector()));
    }

    StrategyDetector(List<StrategyRuleDetector> placementDetectors,
                     List<EliminationDetector> eliminationDetectors) {
        this.placementDetectors = placementDetectors;
        this.eliminationDetectors = eliminationDetectors;
    }

    public StrategyResult detectNextMove(int[][] grid, int[][] solution) {
        for (StrategyRuleDetector detector : placementDetectors) {
            StrategyResult result = detector.detect(grid, solution);
            if (result.found) return result;
        }
        CandidateGrid candidates = CandidateGrid.from(grid);
        for (EliminationDetector detector : eliminationDetectors) {
            CandidateElimination.StrategyHit hit = detector.detect(candidates);
            if (hit.found && !hit.eliminations.isEmpty()) {
                return toStrategyResult(hit);
            }
        }
        return StrategyResult.none();
    }

    private static StrategyResult toStrategyResult(CandidateElimination.StrategyHit hit) {
        CandidateElimination first = hit.eliminations.get(0);
        List<CellPosition> highlights = new ArrayList<>(hit.highlightCells);
        for (CandidateElimination elimination : hit.eliminations) {
            CellPosition pos = new CellPosition(elimination.row, elimination.col);
            if (!contains(highlights, pos)) {
                highlights.add(pos);
            }
        }
        String shortText = displayName(hit.strategyName) + ": remove " + first.value
                + " from row " + (first.row + 1) + ", column " + (first.col + 1) + ".";
        return StrategyResult.elimination(hit.strategyName, first.row, first.col, first.value,
                highlights, shortText, hit.explanation);
    }

    private static boolean contains(List<CellPosition> cells, CellPosition target) {
        for (CellPosition cell : cells) {
            if (cell.row == target.row && cell.col == target.col) {
                return true;
            }
        }
        return false;
    }

    private static String displayName(String strategyName) {
        return strategyName == null ? "" : strategyName.replace('_', ' ');
    }
}
