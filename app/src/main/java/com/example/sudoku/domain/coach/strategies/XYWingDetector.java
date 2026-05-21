package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * XY-Wing: three bi-value cells with candidate sets {x,y}, {x,z}, {y,z}.
 * The pivot sees both wings (shares a unit). Any cell that sees both wings
 * cannot contain z.
 */
public class XYWingDetector implements EliminationDetector {

    private static final String STRATEGY = com.example.sudoku.utils.Constants.STRATEGY_XY_WING;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        // Collect bi-value cells.
        int[] cellIdx = new int[Units.N * Units.N];
        int[] cellMask = new int[Units.N * Units.N];
        int count = 0;
        for (int r = 0; r < Units.N; r++) {
            for (int c = 0; c < Units.N; c++) {
                if (cg.isSolved(r, c)) continue;
                int mask = cg.candidates(r, c);
                if (Integer.bitCount(mask) == 2) {
                    cellIdx[count] = r * Units.N + c;
                    cellMask[count] = mask;
                    count++;
                }
            }
        }

        for (int p = 0; p < count; p++) {
            int pivot = cellIdx[p];
            int pivotMask = cellMask[p];
            int pr = pivot / Units.N;
            int pc = pivot % Units.N;
            for (int w1 = 0; w1 < count; w1++) {
                if (w1 == p) continue;
                int wing1 = cellIdx[w1];
                int wing1Mask = cellMask[w1];
                if (Integer.bitCount(pivotMask & wing1Mask) != 1) continue;
                int wr1 = wing1 / Units.N;
                int wc1 = wing1 % Units.N;
                if (!sharesUnit(pr, pc, wr1, wc1)) continue;
                for (int w2 = w1 + 1; w2 < count; w2++) {
                    if (w2 == p) continue;
                    int wing2 = cellIdx[w2];
                    int wing2Mask = cellMask[w2];
                    if (Integer.bitCount(pivotMask & wing2Mask) != 1) continue;
                    int wr2 = wing2 / Units.N;
                    int wc2 = wing2 % Units.N;
                    if (!sharesUnit(pr, pc, wr2, wc2)) continue;
                    if ((wing1Mask & wing2Mask) != (wing1Mask & wing2Mask)) continue;
                    int common = wing1Mask & wing2Mask;
                    if (Integer.bitCount(common) != 1) continue;
                    if ((common & pivotMask) != 0) continue; // z must not be in pivot
                    int zBit = common;
                    int zDigit = Integer.numberOfTrailingZeros(zBit);

                    List<CandidateElimination> elims = new ArrayList<>();
                    for (int r = 0; r < Units.N; r++) {
                        for (int c = 0; c < Units.N; c++) {
                            if (cg.isSolved(r, c)) continue;
                            if ((r == pr && c == pc) || (r == wr1 && c == wc1) || (r == wr2 && c == wc2)) continue;
                            if ((cg.candidates(r, c) & zBit) == 0) continue;
                            if (sharesUnit(r, c, wr1, wc1) && sharesUnit(r, c, wr2, wc2)) {
                                elims.add(new CandidateElimination(r, c, zDigit));
                            }
                        }
                    }
                    if (!elims.isEmpty()) {
                        List<CellPosition> highlights = new ArrayList<>();
                        highlights.add(new CellPosition(pr, pc));
                        highlights.add(new CellPosition(wr1, wc1));
                        highlights.add(new CellPosition(wr2, wc2));
                        return StrategyHit.hit(STRATEGY, elims, highlights,
                                "XY-Wing eliminates digit " + zDigit + ".");
                    }
                }
            }
        }
        return StrategyHit.none();
    }

    private static boolean sharesUnit(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) return false;
        if (r1 == r2 || c1 == c2) return true;
        return (r1 / Units.B) == (r2 / Units.B) && (c1 / Units.B) == (c2 / Units.B);
    }
}
