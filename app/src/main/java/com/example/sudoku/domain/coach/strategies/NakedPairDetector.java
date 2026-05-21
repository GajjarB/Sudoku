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
 * Two cells in the same row, column, or box share exactly the same pair of
 * candidates. Those two values can be eliminated from all other cells of
 * the unit.
 */
public class NakedPairDetector implements EliminationDetector {

    private static final int N = CandidateGrid.N;
    private static final int B = CandidateGrid.B;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        StrategyHit hit;
        for (int r = 0; r < N; r++) {
            int[] cells = rowCells(r);
            hit = scanUnit(cg, cells);
            if (hit.found) return hit;
        }
        for (int c = 0; c < N; c++) {
            int[] cells = colCells(c);
            hit = scanUnit(cg, cells);
            if (hit.found) return hit;
        }
        for (int b = 0; b < N; b++) {
            int[] cells = boxCells(b);
            hit = scanUnit(cg, cells);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }

    private StrategyHit scanUnit(CandidateGrid cg, int[] cells) {
        for (int i = 0; i < cells.length; i++) {
            int rc1 = cells[i];
            int r1 = rc1 / N;
            int c1 = rc1 % N;
            int mask1 = cg.candidates(r1, c1);
            if (Integer.bitCount(mask1) != 2) continue;
            for (int j = i + 1; j < cells.length; j++) {
                int rc2 = cells[j];
                int r2 = rc2 / N;
                int c2 = rc2 % N;
                if (cg.candidates(r2, c2) != mask1) continue;

                List<CandidateElimination> elims = new ArrayList<>();
                List<CellPosition> highlights = new ArrayList<>();
                highlights.add(new CellPosition(r1, c1));
                highlights.add(new CellPosition(r2, c2));

                for (int k = 0; k < cells.length; k++) {
                    if (k == i || k == j) continue;
                    int r = cells[k] / N;
                    int c = cells[k] % N;
                    int candidates = cg.candidates(r, c);
                    int toRemove = candidates & mask1;
                    if (toRemove == 0) continue;
                    while (toRemove != 0) {
                        int bit = toRemove & -toRemove;
                        elims.add(new CandidateElimination(r, c, Integer.numberOfTrailingZeros(bit)));
                        toRemove &= ~bit;
                    }
                }
                if (!elims.isEmpty()) {
                    return StrategyHit.hit(Constants.STRATEGY_NAKED_PAIR, elims, highlights,
                            "Two cells share the same pair, so those values can be removed elsewhere in the unit.");
                }
            }
        }
        return StrategyHit.none();
    }

    private int[] rowCells(int r) {
        int[] cells = new int[N];
        for (int c = 0; c < N; c++) cells[c] = r * N + c;
        return cells;
    }

    private int[] colCells(int c) {
        int[] cells = new int[N];
        for (int r = 0; r < N; r++) cells[r] = r * N + c;
        return cells;
    }

    private int[] boxCells(int b) {
        int br = (b / B) * B;
        int bc = (b % B) * B;
        int[] cells = new int[N];
        int idx = 0;
        for (int r = br; r < br + B; r++) {
            for (int c = bc; c < bc + B; c++) {
                cells[idx++] = r * N + c;
            }
        }
        return cells;
    }
}
