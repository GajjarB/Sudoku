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
 * For a digit d, find two rows where d appears in exactly the same two
 * columns; d can be eliminated from those columns in every other row.
 * Symmetric form for columns.
 */
public class XWingDetector implements EliminationDetector {

    private static final int N = CandidateGrid.N;

    private static final String STRATEGY = com.example.sudoku.utils.Constants.STRATEGY_X_WING;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        StrategyHit hit = scanRowBased(cg);
        if (hit.found) return hit;
        return scanColBased(cg);
    }

    private StrategyHit scanRowBased(CandidateGrid cg) {
        for (int d = 1; d <= N; d++) {
            int bit = 1 << d;
            int[] rowCols = new int[N];
            for (int r = 0; r < N; r++) {
                int mask = 0;
                for (int c = 0; c < N; c++) {
                    if ((cg.candidates(r, c) & bit) != 0) mask |= 1 << c;
                }
                rowCols[r] = Integer.bitCount(mask) == 2 ? mask : 0;
            }
            for (int r1 = 0; r1 < N; r1++) {
                if (rowCols[r1] == 0) continue;
                for (int r2 = r1 + 1; r2 < N; r2++) {
                    if (rowCols[r2] != rowCols[r1]) continue;
                    int cols = rowCols[r1];
                    int c1 = Integer.numberOfTrailingZeros(cols);
                    int c2 = Integer.numberOfTrailingZeros(cols & (cols - 1));
                    List<CandidateElimination> elims = new ArrayList<>();
                    List<CellPosition> highlights = new ArrayList<>();
                    highlights.add(new CellPosition(r1, c1));
                    highlights.add(new CellPosition(r1, c2));
                    highlights.add(new CellPosition(r2, c1));
                    highlights.add(new CellPosition(r2, c2));
                    for (int r = 0; r < N; r++) {
                        if (r == r1 || r == r2) continue;
                        if ((cg.candidates(r, c1) & bit) != 0) {
                            elims.add(new CandidateElimination(r, c1, d));
                        }
                        if ((cg.candidates(r, c2) & bit) != 0) {
                            elims.add(new CandidateElimination(r, c2, d));
                        }
                    }
                    if (!elims.isEmpty()) {
                        return StrategyHit.hit(STRATEGY, elims, highlights,
                                "X-Wing on digit " + d + " between rows " + (r1 + 1) + " and " + (r2 + 1) + ".");
                    }
                }
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit scanColBased(CandidateGrid cg) {
        for (int d = 1; d <= N; d++) {
            int bit = 1 << d;
            int[] colRows = new int[N];
            for (int c = 0; c < N; c++) {
                int mask = 0;
                for (int r = 0; r < N; r++) {
                    if ((cg.candidates(r, c) & bit) != 0) mask |= 1 << r;
                }
                colRows[c] = Integer.bitCount(mask) == 2 ? mask : 0;
            }
            for (int c1 = 0; c1 < N; c1++) {
                if (colRows[c1] == 0) continue;
                for (int c2 = c1 + 1; c2 < N; c2++) {
                    if (colRows[c2] != colRows[c1]) continue;
                    int rows = colRows[c1];
                    int r1 = Integer.numberOfTrailingZeros(rows);
                    int r2 = Integer.numberOfTrailingZeros(rows & (rows - 1));
                    List<CandidateElimination> elims = new ArrayList<>();
                    List<CellPosition> highlights = new ArrayList<>();
                    highlights.add(new CellPosition(r1, c1));
                    highlights.add(new CellPosition(r1, c2));
                    highlights.add(new CellPosition(r2, c1));
                    highlights.add(new CellPosition(r2, c2));
                    for (int c = 0; c < N; c++) {
                        if (c == c1 || c == c2) continue;
                        if ((cg.candidates(r1, c) & bit) != 0) {
                            elims.add(new CandidateElimination(r1, c, d));
                        }
                        if ((cg.candidates(r2, c) & bit) != 0) {
                            elims.add(new CandidateElimination(r2, c, d));
                        }
                    }
                    if (!elims.isEmpty()) {
                        return StrategyHit.hit(STRATEGY, elims, highlights,
                                "X-Wing on digit " + d + " between columns " + (c1 + 1) + " and " + (c2 + 1) + ".");
                    }
                }
            }
        }
        return StrategyHit.none();
    }
}
