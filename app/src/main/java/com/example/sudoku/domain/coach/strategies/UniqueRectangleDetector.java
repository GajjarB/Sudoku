package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Unique Rectangle, Type 1.
 *
 * A rectangle spans two rows and two columns and lies inside exactly two
 * boxes. If three of its corners are bi-value cells with the same pair
 * {a, b} and the fourth corner contains those candidates plus extras,
 * the extras MUST be the truth — otherwise the puzzle has two solutions
 * (swap a/b on the rectangle). The {a, b} candidates can be removed from
 * the fourth corner.
 */
public class UniqueRectangleDetector implements EliminationDetector {

    private static final String STRATEGY = "UNIQUE_RECTANGLE";

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int r1 = 0; r1 < Units.N - 1; r1++) {
            for (int r2 = r1 + 1; r2 < Units.N; r2++) {
                for (int c1 = 0; c1 < Units.N - 1; c1++) {
                    for (int c2 = c1 + 1; c2 < Units.N; c2++) {
                        // Rectangle must span exactly two boxes.
                        int boxes = 0;
                        boxes |= 1 << CandidateGrid.boxIndex(r1, c1);
                        boxes |= 1 << CandidateGrid.boxIndex(r1, c2);
                        boxes |= 1 << CandidateGrid.boxIndex(r2, c1);
                        boxes |= 1 << CandidateGrid.boxIndex(r2, c2);
                        if (Integer.bitCount(boxes) != 2) continue;

                        int[][] corners = {
                                {r1, c1}, {r1, c2}, {r2, c1}, {r2, c2}
                        };
                        StrategyHit hit = checkType1(cg, corners);
                        if (hit.found) return hit;
                    }
                }
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit checkType1(CandidateGrid cg, int[][] corners) {
        for (int extraIdx = 0; extraIdx < 4; extraIdx++) {
            int[] biCornerMasks = new int[3];
            int[] biCornerRC = new int[3];
            int idx = 0;
            boolean ok = true;
            int pair = -1;
            for (int i = 0; i < 4; i++) {
                int r = corners[i][0];
                int c = corners[i][1];
                if (cg.isSolved(r, c)) { ok = false; break; }
                int mask = cg.candidates(r, c);
                if (i == extraIdx) continue;
                if (Integer.bitCount(mask) != 2) { ok = false; break; }
                if (pair == -1) pair = mask;
                else if (pair != mask) { ok = false; break; }
                biCornerMasks[idx] = mask;
                biCornerRC[idx] = r * Units.N + c;
                idx++;
            }
            if (!ok || pair == -1) continue;

            int er = corners[extraIdx][0];
            int ec = corners[extraIdx][1];
            int extraMask = cg.candidates(er, ec);
            if ((extraMask & pair) != pair) continue;
            int toRemove = extraMask & pair;
            if (toRemove == 0) continue;

            List<CandidateElimination> elims = new ArrayList<>();
            int bits = toRemove;
            while (bits != 0) {
                int low = bits & -bits;
                elims.add(new CandidateElimination(er, ec, Integer.numberOfTrailingZeros(low)));
                bits &= ~low;
            }
            List<CellPosition> highlights = new ArrayList<>();
            for (int[] corner : corners) {
                highlights.add(new CellPosition(corner[0], corner[1]));
            }
            return StrategyHit.hit(STRATEGY, elims, highlights,
                    "Unique Rectangle: removing the bi-value pair from the extra corner avoids a deadly pattern.");
        }
        return StrategyHit.none();
    }
}
