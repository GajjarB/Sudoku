package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Simple Coloring on a single digit.
 *
 * Build conjugate-pair chains (units where the digit appears in exactly
 * two cells). Two-color the chain. Two rules:
 *   - Rule 1 (two-of-the-same-color sees each other): contradiction —
 *     all cells of that color cannot be true; eliminate the digit from
 *     all cells of that color.
 *   - Rule 2 (off-chain cell sees both colors): eliminate the digit from
 *     that cell.
 */
public class SimpleColoringDetector implements EliminationDetector {

    private static final String STRATEGY = "SIMPLE_COLORING";

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int d = 1; d <= Units.N; d++) {
            int bit = 1 << d;
            int[] color = new int[Units.N * Units.N];
            for (int i = 0; i < color.length; i++) color[i] = 0; // 0 = unvisited
            List<int[]> conjugates = collectConjugatePairs(cg, bit);
            if (conjugates.isEmpty()) continue;

            int[][] adjacency = buildAdjacency(conjugates);

            for (int start = 0; start < color.length; start++) {
                if (color[start] != 0) continue;
                int r0 = start / Units.N;
                int c0 = start % Units.N;
                if (cg.isSolved(r0, c0) || (cg.candidates(r0, c0) & bit) == 0) continue;
                if (adjacency[start] == null || adjacency[start].length == 0) continue;

                List<Integer> colorAcells = new ArrayList<>();
                List<Integer> colorBcells = new ArrayList<>();
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(start);
                color[start] = 1;
                while (!stack.isEmpty()) {
                    int cur = stack.pop();
                    if (color[cur] == 1) colorAcells.add(cur);
                    else colorBcells.add(cur);
                    for (int nb : adjacency[cur]) {
                        if (color[nb] == 0) {
                            color[nb] = color[cur] == 1 ? 2 : 1;
                            stack.push(nb);
                        }
                    }
                }

                StrategyHit hit = applyRule1(cg, d, bit, colorAcells, colorBcells);
                if (hit.found) return hit;
                hit = applyRule2(cg, d, bit, colorAcells, colorBcells);
                if (hit.found) return hit;
            }
        }
        return StrategyHit.none();
    }

    private List<int[]> collectConjugatePairs(CandidateGrid cg, int bit) {
        List<int[]> pairs = new ArrayList<>();
        for (int unitType = 0; unitType < 3; unitType++) {
            for (int u = 0; u < Units.N; u++) {
                int[] cells = unitType == 0 ? Units.row(u)
                        : unitType == 1 ? Units.col(u) : Units.box(u);
                int count = 0;
                int a = -1;
                int b = -1;
                for (int rc : cells) {
                    int r = rc / Units.N;
                    int c = rc % Units.N;
                    if (cg.isSolved(r, c)) continue;
                    if ((cg.candidates(r, c) & bit) != 0) {
                        if (a == -1) a = rc;
                        else b = rc;
                        count++;
                    }
                }
                if (count == 2) pairs.add(new int[]{a, b});
            }
        }
        return pairs;
    }

    private int[][] buildAdjacency(List<int[]> pairs) {
        int[] degree = new int[Units.N * Units.N];
        for (int[] p : pairs) {
            degree[p[0]]++;
            degree[p[1]]++;
        }
        int[][] adj = new int[Units.N * Units.N][];
        int[] idx = new int[Units.N * Units.N];
        for (int i = 0; i < adj.length; i++) {
            if (degree[i] > 0) adj[i] = new int[degree[i]];
        }
        for (int[] p : pairs) {
            adj[p[0]][idx[p[0]]++] = p[1];
            adj[p[1]][idx[p[1]]++] = p[0];
        }
        return adj;
    }

    private StrategyHit applyRule1(CandidateGrid cg, int digit, int bit,
                                    List<Integer> colorA, List<Integer> colorB) {
        StrategyHit hit = checkColorContradiction(cg, digit, bit, colorA, colorB);
        if (hit.found) return hit;
        return checkColorContradiction(cg, digit, bit, colorB, colorA);
    }

    private StrategyHit checkColorContradiction(CandidateGrid cg, int digit, int bit,
                                                 List<Integer> sameColor, List<Integer> ignored) {
        for (int i = 0; i < sameColor.size(); i++) {
            for (int j = i + 1; j < sameColor.size(); j++) {
                int a = sameColor.get(i);
                int b = sameColor.get(j);
                if (sharesUnit(a / Units.N, a % Units.N, b / Units.N, b % Units.N)) {
                    List<CandidateElimination> elims = new ArrayList<>();
                    List<CellPosition> highlights = new ArrayList<>();
                    for (int rc : sameColor) {
                        int r = rc / Units.N;
                        int c = rc % Units.N;
                        elims.add(new CandidateElimination(r, c, digit));
                        highlights.add(new CellPosition(r, c));
                    }
                    return StrategyHit.hit(STRATEGY, elims, highlights,
                            "Simple Coloring: two cells of the same color see each other.");
                }
            }
        }
        return StrategyHit.none();
    }

    private StrategyHit applyRule2(CandidateGrid cg, int digit, int bit,
                                    List<Integer> colorA, List<Integer> colorB) {
        List<CandidateElimination> elims = new ArrayList<>();
        for (int r = 0; r < Units.N; r++) {
            for (int c = 0; c < Units.N; c++) {
                if (cg.isSolved(r, c)) continue;
                int rc = r * Units.N + c;
                if (colorA.contains(rc) || colorB.contains(rc)) continue;
                if ((cg.candidates(r, c) & bit) == 0) continue;
                boolean seesA = false;
                boolean seesB = false;
                for (int a : colorA) {
                    if (sharesUnit(r, c, a / Units.N, a % Units.N)) { seesA = true; break; }
                }
                if (!seesA) continue;
                for (int b : colorB) {
                    if (sharesUnit(r, c, b / Units.N, b % Units.N)) { seesB = true; break; }
                }
                if (seesB) {
                    elims.add(new CandidateElimination(r, c, digit));
                }
            }
        }
        if (!elims.isEmpty()) {
            List<CellPosition> highlights = new ArrayList<>();
            for (int rc : colorA) highlights.add(new CellPosition(rc / Units.N, rc % Units.N));
            for (int rc : colorB) highlights.add(new CellPosition(rc / Units.N, rc % Units.N));
            return StrategyHit.hit(STRATEGY, elims, highlights,
                    "Simple Coloring: cell sees both colors of digit " + digit + ".");
        }
        return StrategyHit.none();
    }

    private static boolean sharesUnit(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) return false;
        if (r1 == r2 || c1 == c2) return true;
        return (r1 / Units.B) == (r2 / Units.B) && (c1 / Units.B) == (c2 / Units.B);
    }
}
