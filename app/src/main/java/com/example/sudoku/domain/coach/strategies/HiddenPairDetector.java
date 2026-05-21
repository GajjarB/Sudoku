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
 * Two candidate values both appear only in the same two cells of a unit;
 * any other candidates in those cells can be cleared.
 */
public class HiddenPairDetector implements EliminationDetector {

    private static final int N = CandidateGrid.N;
    private static final int B = CandidateGrid.B;

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        StrategyHit hit;
        for (int r = 0; r < N; r++) {
            hit = scanUnit(cg, rowCells(r));
            if (hit.found) return hit;
        }
        for (int c = 0; c < N; c++) {
            hit = scanUnit(cg, colCells(c));
            if (hit.found) return hit;
        }
        for (int b = 0; b < N; b++) {
            hit = scanUnit(cg, boxCells(b));
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }

    private StrategyHit scanUnit(CandidateGrid cg, int[] cells) {
        // For each digit pair, locate the cells that allow that digit.
        for (int d1 = 1; d1 <= N; d1++) {
            int bit1 = 1 << d1;
            int[] cellsWithD1 = cellsContaining(cg, cells, bit1);
            if (cellsWithD1.length != 2) continue;
            for (int d2 = d1 + 1; d2 <= N; d2++) {
                int bit2 = 1 << d2;
                int[] cellsWithD2 = cellsContaining(cg, cells, bit2);
                if (cellsWithD2.length != 2) continue;
                if (cellsWithD2[0] != cellsWithD1[0] || cellsWithD2[1] != cellsWithD1[1]) continue;

                int pairMask = bit1 | bit2;
                List<CandidateElimination> elims = new ArrayList<>();
                List<CellPosition> highlights = new ArrayList<>();
                for (int rc : cellsWithD1) {
                    int r = rc / N;
                    int c = rc % N;
                    highlights.add(new CellPosition(r, c));
                    int extra = cg.candidates(r, c) & ~pairMask;
                    while (extra != 0) {
                        int bit = extra & -extra;
                        elims.add(new CandidateElimination(r, c, Integer.numberOfTrailingZeros(bit)));
                        extra &= ~bit;
                    }
                }
                if (!elims.isEmpty()) {
                    return StrategyHit.hit(Constants.STRATEGY_HIDDEN_PAIR, elims, highlights,
                            "Only two cells in the unit can hold this digit pair; other candidates in those cells are eliminated.");
                }
            }
        }
        return StrategyHit.none();
    }

    private int[] cellsContaining(CandidateGrid cg, int[] cells, int bit) {
        int count = 0;
        int[] tmp = new int[N];
        for (int rc : cells) {
            int r = rc / N;
            int c = rc % N;
            if ((cg.candidates(r, c) & bit) != 0) {
                tmp[count++] = rc;
            }
        }
        int[] out = new int[count];
        System.arraycopy(tmp, 0, out, 0, count);
        return out;
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
