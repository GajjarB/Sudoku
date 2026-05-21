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
 * Within a 3x3 box, all candidate cells for a digit lie in the same row
 * (or column). That digit can be eliminated from the rest of the row/col.
 */
public class PointingPairDetector implements EliminationDetector {

    private static final int N = CandidateGrid.N;
    private static final int B = CandidateGrid.B;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int box = 0; box < N; box++) {
            int boxR = (box / B) * B;
            int boxC = (box % B) * B;
            for (int d = 1; d <= N; d++) {
                int bit = 1 << d;
                int rowsMask = 0;
                int colsMask = 0;
                int candidatesInBox = 0;
                List<CellPosition> spots = new ArrayList<>();
                for (int r = boxR; r < boxR + B; r++) {
                    for (int c = boxC; c < boxC + B; c++) {
                        if ((cg.candidates(r, c) & bit) != 0) {
                            rowsMask |= 1 << r;
                            colsMask |= 1 << c;
                            candidatesInBox++;
                            spots.add(new CellPosition(r, c));
                        }
                    }
                }
                if (candidatesInBox < 2 || candidatesInBox > 3) continue;

                if (Integer.bitCount(rowsMask) == 1) {
                    int row = Integer.numberOfTrailingZeros(rowsMask);
                    List<CandidateElimination> elims = collectRowElsewhere(cg, row, boxC, bit, d);
                    if (!elims.isEmpty()) {
                        return StrategyHit.hit(Constants.STRATEGY_POINTING_PAIR, elims, spots,
                                "Digit " + d + " in this box is confined to one row.");
                    }
                }
                if (Integer.bitCount(colsMask) == 1) {
                    int col = Integer.numberOfTrailingZeros(colsMask);
                    List<CandidateElimination> elims = collectColElsewhere(cg, col, boxR, bit, d);
                    if (!elims.isEmpty()) {
                        return StrategyHit.hit(Constants.STRATEGY_POINTING_PAIR, elims, spots,
                                "Digit " + d + " in this box is confined to one column.");
                    }
                }
            }
        }
        return StrategyHit.none();
    }

    private List<CandidateElimination> collectRowElsewhere(CandidateGrid cg, int row, int boxC, int bit, int digit) {
        List<CandidateElimination> elims = new ArrayList<>();
        for (int c = 0; c < N; c++) {
            if (c >= boxC && c < boxC + B) continue;
            if ((cg.candidates(row, c) & bit) != 0) {
                elims.add(new CandidateElimination(row, c, digit));
            }
        }
        return elims;
    }

    private List<CandidateElimination> collectColElsewhere(CandidateGrid cg, int col, int boxR, int bit, int digit) {
        List<CandidateElimination> elims = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            if (r >= boxR && r < boxR + B) continue;
            if ((cg.candidates(r, col) & bit) != 0) {
                elims.add(new CandidateElimination(r, col, digit));
            }
        }
        return elims;
    }
}
