package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;
import com.example.sudoku.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Within a row (or column), all candidate cells for a digit lie inside a
 * single box. That digit can be eliminated from the rest of the box.
 */
public class BoxLineReductionDetector implements EliminationDetector {

    private static final int N = CandidateGrid.N;
    private static final int B = CandidateGrid.B;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int r = 0; r < N; r++) {
            StrategyHit hit = scanRow(cg, r);
            if (hit.found) return hit;
        }
        for (int c = 0; c < N; c++) {
            StrategyHit hit = scanCol(cg, c);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }

    private StrategyHit scanRow(CandidateGrid cg, int row) {
        for (int d = 1; d <= N; d++) {
            int bit = 1 << d;
            int boxes = 0;
            List<CellPosition> spots = new ArrayList<>();
            int boxIndex = -1;
            for (int c = 0; c < N; c++) {
                if ((cg.candidates(row, c) & bit) != 0) {
                    int box = CandidateGrid.boxIndex(row, c);
                    int boxMask = 1 << box;
                    boxes |= boxMask;
                    boxIndex = box;
                    spots.add(new CellPosition(row, c));
                }
            }
            if (Integer.bitCount(boxes) != 1 || spots.size() < 2) continue;
            int br = (boxIndex / B) * B;
            int bc = (boxIndex % B) * B;
            List<CandidateElimination> elims = new ArrayList<>();
            for (int r = br; r < br + B; r++) {
                if (r == row) continue;
                for (int c = bc; c < bc + B; c++) {
                    if ((cg.candidates(r, c) & bit) != 0) {
                        elims.add(new CandidateElimination(r, c, d));
                    }
                }
            }
            if (!elims.isEmpty()) {
                return StrategyHit.hit(Constants.STRATEGY_BOX_LINE_REDUCTION, elims, spots,
                        "Digit " + d + " in this row only fits inside one box.");
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit scanCol(CandidateGrid cg, int col) {
        for (int d = 1; d <= N; d++) {
            int bit = 1 << d;
            int boxes = 0;
            List<CellPosition> spots = new ArrayList<>();
            int boxIndex = -1;
            for (int r = 0; r < N; r++) {
                if ((cg.candidates(r, col) & bit) != 0) {
                    int box = CandidateGrid.boxIndex(r, col);
                    boxes |= 1 << box;
                    boxIndex = box;
                    spots.add(new CellPosition(r, col));
                }
            }
            if (Integer.bitCount(boxes) != 1 || spots.size() < 2) continue;
            int br = (boxIndex / B) * B;
            int bc = (boxIndex % B) * B;
            List<CandidateElimination> elims = new ArrayList<>();
            for (int r = br; r < br + B; r++) {
                for (int c = bc; c < bc + B; c++) {
                    if (c == col) continue;
                    if ((cg.candidates(r, c) & bit) != 0) {
                        elims.add(new CandidateElimination(r, c, d));
                    }
                }
            }
            if (!elims.isEmpty()) {
                return StrategyHit.hit(Constants.STRATEGY_BOX_LINE_REDUCTION, elims, spots,
                        "Digit " + d + " in this column only fits inside one box.");
            }
        }
        return StrategyHit.none();
    }
}
