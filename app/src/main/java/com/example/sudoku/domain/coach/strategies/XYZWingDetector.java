package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * XYZ-Wing: pivot cell has 3 candidates {x,y,z}. Two wing cells are
 * bi-value: {x,z} and {y,z}. Each wing must share a unit with the pivot.
 * Any cell that sees all three (pivot + both wings) cannot contain z.
 */
public class XYZWingDetector implements EliminationDetector {

    private static final String STRATEGY = "XYZ_WING";

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int pr = 0; pr < Units.N; pr++) {
            for (int pc = 0; pc < Units.N; pc++) {
                if (cg.isSolved(pr, pc)) continue;
                int pivotMask = cg.candidates(pr, pc);
                if (Integer.bitCount(pivotMask) != 3) continue;

                StrategyHit hit = findWing1(cg, pr, pc, pivotMask);
                if (hit.found) {
                    return hit;
                }
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit findWing1(CandidateGrid cg, int pr, int pc, int pivotMask) {
        for (int wr1 = 0; wr1 < Units.N; wr1++) {
            for (int wc1 = 0; wc1 < Units.N; wc1++) {
                if (cg.isSolved(wr1, wc1)) continue;
                if (wr1 == pr && wc1 == pc) continue;
                if (!sharesUnit(pr, pc, wr1, wc1)) continue;
                int wing1Mask = cg.candidates(wr1, wc1);
                if (Integer.bitCount(wing1Mask) != 2) continue;
                if ((wing1Mask & pivotMask) != wing1Mask) continue;

                StrategyHit hit = findWing2(cg, pr, pc, pivotMask, wr1, wc1, wing1Mask);
                if (hit.found) {
                    return hit;
                }
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit findWing2(CandidateGrid cg, int pr, int pc, int pivotMask, int wr1, int wc1, int wing1Mask) {
        for (int wr2 = 0; wr2 < Units.N; wr2++) {
            for (int wc2 = 0; wc2 < Units.N; wc2++) {
                if (cg.isSolved(wr2, wc2)) continue;
                if ((wr2 == pr && wc2 == pc) || (wr2 == wr1 && wc2 == wc1)) continue;
                if (!sharesUnit(pr, pc, wr2, wc2)) continue;
                int wing2Mask = cg.candidates(wr2, wc2);
                if (Integer.bitCount(wing2Mask) != 2) continue;
                if ((wing2Mask & pivotMask) != wing2Mask) continue;

                int common = wing1Mask & wing2Mask;
                if (Integer.bitCount(common) != 1) continue;
                if ((wing1Mask | wing2Mask) != pivotMask) continue;

                int zBit = common;
                int zDigit = Integer.numberOfTrailingZeros(zBit);

                List<CandidateElimination> elims = findEliminations(cg, pr, pc, wr1, wc1, wr2, wc2, zBit, zDigit);
                if (!elims.isEmpty()) {
                    List<CellPosition> highlights = new ArrayList<>();
                    highlights.add(new CellPosition(pr, pc));
                    highlights.add(new CellPosition(wr1, wc1));
                    highlights.add(new CellPosition(wr2, wc2));
                    return StrategyHit.hit(STRATEGY, elims, highlights,
                            "XYZ-Wing eliminates digit " + zDigit + ".");
                }
            }
        }
        return StrategyHit.none();
    }

    private List<CandidateElimination> findEliminations(CandidateGrid cg, int pr, int pc,
                                                        int wr1, int wc1, int wr2, int wc2,
                                                        int zBit, int zDigit) {
        List<CandidateElimination> elims = new ArrayList<>();
        for (int r = 0; r < Units.N; r++) {
            for (int c = 0; c < Units.N; c++) {
                if (cg.isSolved(r, c)) continue;
                if ((r == pr && c == pc) || (r == wr1 && c == wc1) || (r == wr2 && c == wc2)) continue;
                if ((cg.candidates(r, c) & zBit) == 0) continue;
                if (sharesUnit(r, c, pr, pc)
                        && sharesUnit(r, c, wr1, wc1)
                        && sharesUnit(r, c, wr2, wc2)) {
                    elims.add(new CandidateElimination(r, c, zDigit));
                }
            }
        }
        return elims;
    }

    private static boolean sharesUnit(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) return false;
        if (r1 == r2 || c1 == c2) return true;
        return (r1 / Units.B) == (r2 / Units.B) && (c1 / Units.B) == (c2 / Units.B);
    }
}
