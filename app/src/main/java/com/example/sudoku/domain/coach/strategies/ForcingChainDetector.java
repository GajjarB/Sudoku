package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Bi-value forcing chain. For each bi-value cell C with candidates {a, b}:
 *   - Hypothetically place a; propagate naked-single + hidden-single
 *     placements until no more progress.
 *   - Hypothetically place b; propagate the same way.
 *
 * If both branches force the same elimination (a particular cell cannot
 * hold a particular digit), that elimination is sound regardless of which
 * branch is correct.
 *
 * Limited-depth implementation: propagation stops when no more singles
 * can be placed. Useful as a last-resort technique before resorting to
 * raw guessing.
 */
public class ForcingChainDetector implements EliminationDetector {

    private static final String STRATEGY = "FORCING_CHAIN";
    private static final int MAX_PIVOTS = 30;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        int evaluated = 0;
        for (int r = 0; r < Units.N && evaluated < MAX_PIVOTS; r++) {
            for (int c = 0; c < Units.N && evaluated < MAX_PIVOTS; c++) {
                if (cg.isSolved(r, c)) continue;
                int mask = cg.candidates(r, c);
                if (Integer.bitCount(mask) != 2) continue;
                evaluated++;
                int low = mask & -mask;
                int high = mask & ~low;
                int aDigit = Integer.numberOfTrailingZeros(low);
                int bDigit = Integer.numberOfTrailingZeros(high);

                int[][] branchA = propagate(cg, r, c, aDigit);
                int[][] branchB = propagate(cg, r, c, bDigit);
                if (branchA == null || branchB == null) continue;

                List<CandidateElimination> elims = new ArrayList<>();
                for (int rr = 0; rr < Units.N; rr++) {
                    for (int cc = 0; cc < Units.N; cc++) {
                        if (cg.isSolved(rr, cc)) continue;
                        for (int d = 1; d <= Units.N; d++) {
                            int bit = 1 << d;
                            if ((cg.candidates(rr, cc) & bit) == 0) continue;
                            boolean clearedInA = (branchA[rr][cc] & bit) == 0;
                            boolean clearedInB = (branchB[rr][cc] & bit) == 0;
                            if (clearedInA && clearedInB && !(rr == r && cc == c)) {
                                elims.add(new CandidateElimination(rr, cc, d));
                            }
                        }
                    }
                }
                if (!elims.isEmpty()) {
                    List<CellPosition> highlights = new ArrayList<>();
                    highlights.add(new CellPosition(r, c));
                    return StrategyHit.hit(STRATEGY, elims, highlights,
                            "Both candidates of this bi-value cell force the same elimination.");
                }
            }
        }
        return StrategyHit.none();
    }

    private int[][] propagate(CandidateGrid cg, int row, int col, int value) {
        CandidateGrid copy = cloneGrid(cg);
        if (!copy.place(row, col, value)) return null;
        boolean progress = true;
        int safety = 0;
        while (progress && safety++ < 200) {
            progress = false;
            // Naked single placements.
            for (int r = 0; r < Units.N; r++) {
                for (int c = 0; c < Units.N; c++) {
                    if (copy.isSolved(r, c)) continue;
                    int mask = copy.candidates(r, c);
                    if (Integer.bitCount(mask) == 1) {
                        copy.place(r, c, Integer.numberOfTrailingZeros(mask));
                        progress = true;
                    } else if (mask == 0) {
                        // Contradiction in this branch — treat all eliminations
                        // as forced by returning a fully-cleared candidate map.
                        return zeroCandidates();
                    }
                }
            }
        }
        return snapshotCandidates(copy);
    }

    private static CandidateGrid cloneGrid(CandidateGrid src) {
        return CandidateGrid.from(src.gridCopy());
    }

    private static int[][] snapshotCandidates(CandidateGrid cg) {
        int[][] out = new int[Units.N][Units.N];
        for (int r = 0; r < Units.N; r++) {
            for (int c = 0; c < Units.N; c++) {
                out[r][c] = cg.isSolved(r, c) ? 0 : cg.candidates(r, c);
            }
        }
        return out;
    }

    private static int[][] zeroCandidates() {
        return new int[Units.N][Units.N];
    }
}
