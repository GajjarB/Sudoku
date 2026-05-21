package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Generalized naked subset: K unsolved cells in a unit whose combined
 * candidates total exactly K distinct digits. Those digits are removed
 * from every other cell in the unit.
 *
 * K=2 → naked pair (handled by dedicated detector for friendlier hints).
 * K=3 → naked triple.
 * K=4 → naked quad.
 */
public class NakedSubsetDetector implements EliminationDetector {

    private final int size;
    private final String strategyName;

    public NakedSubsetDetector(int size, String strategyName) {
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
        // Collect unsolved cells with at most `size` candidates.
        int[] candidates = new int[Units.N];
        int[] idx = new int[Units.N];
        int count = 0;
        for (int rc : cells) {
            int r = rc / Units.N;
            int c = rc % Units.N;
            int mask = cg.candidates(r, c);
            int bits = Integer.bitCount(mask);
            if (bits >= 2 && bits <= size) {
                candidates[count] = mask;
                idx[count] = rc;
                count++;
            }
        }
        int[] combo = new int[size];
        return enumerate(cg, cells, candidates, idx, count, combo, 0, 0);
    }

    private StrategyHit enumerate(CandidateGrid cg, int[] cells, int[] candidates,
                                   int[] idx, int count, int[] combo, int start, int depth) {
        if (depth == size) {
            int union = 0;
            for (int i = 0; i < size; i++) union |= candidates[combo[i]];
            if (Integer.bitCount(union) != size) return StrategyHit.none();

            List<CandidateElimination> elims = new ArrayList<>();
            List<CellPosition> highlights = new ArrayList<>();
            boolean[] inSubset = new boolean[count];
            for (int i = 0; i < size; i++) {
                inSubset[combo[i]] = true;
                int rc = idx[combo[i]];
                highlights.add(new CellPosition(rc / Units.N, rc % Units.N));
            }
            for (int rc : cells) {
                int r = rc / Units.N;
                int c = rc % Units.N;
                if (cg.isSolved(r, c)) continue;
                boolean skip = false;
                for (int i = 0; i < size; i++) {
                    if (idx[combo[i]] == rc) { skip = true; break; }
                }
                if (skip) continue;
                int toRemove = cg.candidates(r, c) & union;
                while (toRemove != 0) {
                    int bit = toRemove & -toRemove;
                    elims.add(new CandidateElimination(r, c, Integer.numberOfTrailingZeros(bit)));
                    toRemove &= ~bit;
                }
            }
            if (!elims.isEmpty()) {
                return StrategyHit.hit(strategyName, elims, highlights,
                        size + " cells share " + size + " candidate values, removing them from peers.");
            }
            return StrategyHit.none();
        }
        for (int i = start; i < count; i++) {
            combo[depth] = i;
            StrategyHit hit = enumerate(cg, cells, candidates, idx, count, combo, i + 1, depth + 1);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }
}
