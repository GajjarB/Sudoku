package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Generalized hidden subset: K digits whose combined candidate cells in a
 * unit total exactly K cells. Those K cells can have all other candidates
 * removed.
 */
public class HiddenSubsetDetector implements EliminationDetector {

    private final int size;
    private final String strategyName;

    public HiddenSubsetDetector(int size, String strategyName) {
        this.size = size;
        this.strategyName = strategyName;
    }

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        StrategyHit hit;
        for (int r = 0; r < Units.N; r++) {
            hit = scanUnit(cg, Units.row(r));
            if (hit.found) return hit;
        }
        for (int c = 0; c < Units.N; c++) {
            hit = scanUnit(cg, Units.col(c));
            if (hit.found) return hit;
        }
        for (int b = 0; b < Units.N; b++) {
            hit = scanUnit(cg, Units.box(b));
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }

    private StrategyHit scanUnit(CandidateGrid cg, int[] cells) {
        // For each digit 1..9, find which cells in the unit can host it.
        int[] digitCellMask = new int[10];
        for (int d = 1; d <= Units.N; d++) {
            int bit = 1 << d;
            int mask = 0;
            for (int i = 0; i < cells.length; i++) {
                int rc = cells[i];
                int r = rc / Units.N;
                int c = rc % Units.N;
                if (cg.isSolved(r, c)) continue;
                if ((cg.candidates(r, c) & bit) != 0) mask |= 1 << i;
            }
            digitCellMask[d] = mask;
        }

        int[] eligibleDigits = new int[Units.N];
        int count = 0;
        for (int d = 1; d <= Units.N; d++) {
            int bits = Integer.bitCount(digitCellMask[d]);
            if (bits >= 2 && bits <= size) {
                eligibleDigits[count++] = d;
            }
        }
        int[] combo = new int[size];
        return enumerate(cg, cells, digitCellMask, eligibleDigits, count, combo, 0, 0);
    }

    private StrategyHit enumerate(CandidateGrid cg, int[] cells, int[] digitCellMask,
                                   int[] eligible, int count, int[] combo, int start, int depth) {
        if (depth == size) {
            int unionCells = 0;
            int digitMask = 0;
            for (int i = 0; i < size; i++) {
                int d = eligible[combo[i]];
                unionCells |= digitCellMask[d];
                digitMask |= 1 << d;
            }
            if (Integer.bitCount(unionCells) != size) return StrategyHit.none();

            List<CandidateElimination> elims = new ArrayList<>();
            List<CellPosition> highlights = new ArrayList<>();
            int bits = unionCells;
            while (bits != 0) {
                int lowBit = bits & -bits;
                int cellIdx = Integer.numberOfTrailingZeros(lowBit);
                int rc = cells[cellIdx];
                int r = rc / Units.N;
                int c = rc % Units.N;
                highlights.add(new CellPosition(r, c));
                int extra = cg.candidates(r, c) & ~digitMask;
                while (extra != 0) {
                    int eBit = extra & -extra;
                    elims.add(new CandidateElimination(r, c, Integer.numberOfTrailingZeros(eBit)));
                    extra &= ~eBit;
                }
                bits &= ~lowBit;
            }
            if (!elims.isEmpty()) {
                return StrategyHit.hit(strategyName, elims, highlights,
                        size + " digits are confined to " + size + " cells; other candidates removed.");
            }
            return StrategyHit.none();
        }
        for (int i = start; i < count; i++) {
            combo[depth] = i;
            StrategyHit hit = enumerate(cg, cells, digitCellMask, eligible, count, combo, i + 1, depth + 1);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }
}
