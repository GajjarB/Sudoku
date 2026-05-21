package com.example.sudoku.domain.engine;

import static com.example.sudoku.domain.engine.SudokuConstants.BOX_SIZE;
import static com.example.sudoku.domain.engine.SudokuConstants.GRID_SIZE;

/**
 * Knuth's Algorithm X with Dancing Links, applied to the exact-cover
 * formulation of 9x9 Sudoku.
 *
 * Four constraint groups (324 columns total):
 *   - Each cell must have exactly one digit  (81 columns)
 *   - Each row must contain each digit once  (81 columns)
 *   - Each column must contain each digit    (81 columns)
 *   - Each box must contain each digit       (81 columns)
 *
 * 729 rows = 9x9x9 (cell, digit) candidate placements.
 *
 * Used for fast uniqueness checks during generation. Roughly an order of
 * magnitude faster than naive backtracking for hard puzzles.
 */
public class DancingLinksSolver {

    private static final int N = GRID_SIZE;
    private static final int B = BOX_SIZE;
    private static final int CONSTRAINTS = 4 * N * N; // 324
    private static final int CANDIDATES = N * N * N;  // 729

    private Node header;
    private Node[] solutionStack;
    private int solutionCount;
    private int maxSolutions;
    private int[] resultGrid;

    /**
     * Solve a single solution. Returns null if unsolvable.
     */
    public int[][] solve(int[][] grid) {
        build(grid);
        maxSolutions = 1;
        solutionCount = 0;
        solutionStack = new Node[CANDIDATES];
        resultGrid = new int[N * N];
        search(0);
        if (solutionCount == 0) return null;
        return toGrid(resultGrid);
    }

    /**
     * Count solutions up to maxCount (stops early when reached).
     */
    public int countSolutions(int[][] grid, int maxCount) {
        build(grid);
        this.maxSolutions = maxCount;
        this.solutionCount = 0;
        this.solutionStack = new Node[CANDIDATES];
        this.resultGrid = new int[N * N];
        search(0);
        return solutionCount;
    }

    public boolean hasUniqueSolution(int[][] grid) {
        return countSolutions(grid, 2) == 1;
    }

    // ─── DLX core ───────────────────────────────────────────────────────

    private void search(int depth) {
        if (solutionCount >= maxSolutions) return;
        if (header.right == header) {
            solutionCount++;
            for (int i = 0; i < depth; i++) {
                Node n = solutionStack[i];
                int rowIndex = n.rowId;
                int r = rowIndex / (N * N);
                int c = (rowIndex / N) % N;
                int d = rowIndex % N + 1;
                resultGrid[r * N + c] = d;
            }
            return;
        }

        ColumnNode chosen = chooseColumn();
        cover(chosen);

        for (Node r = chosen.down; r != chosen; r = r.down) {
            solutionStack[depth] = r;
            for (Node j = r.right; j != r; j = j.right) cover(j.column);

            search(depth + 1);
            if (solutionCount >= maxSolutions) {
                uncover(chosen);
                return;
            }

            for (Node j = r.left; j != r; j = j.left) uncover(j.column);
        }
        uncover(chosen);
    }

    private ColumnNode chooseColumn() {
        ColumnNode best = (ColumnNode) header.right;
        int min = best.size;
        for (Node c = header.right; c != header; c = c.right) {
            ColumnNode col = (ColumnNode) c;
            if (col.size < min) {
                min = col.size;
                best = col;
            }
        }
        return best;
    }

    private void cover(ColumnNode col) {
        col.right.left = col.left;
        col.left.right = col.right;
        for (Node i = col.down; i != col; i = i.down) {
            for (Node j = i.right; j != i; j = j.right) {
                j.down.up = j.up;
                j.up.down = j.down;
                j.column.size--;
            }
        }
    }

    private void uncover(ColumnNode col) {
        for (Node i = col.up; i != col; i = i.up) {
            for (Node j = i.left; j != i; j = j.left) {
                j.column.size++;
                j.down.up = j;
                j.up.down = j;
            }
        }
        col.right.left = col;
        col.left.right = col;
    }

    // ─── matrix construction ────────────────────────────────────────────

    private void build(int[][] grid) {
        ColumnNode[] columns = new ColumnNode[CONSTRAINTS];
        header = new ColumnNode("h");
        ColumnNode prev = (ColumnNode) header;
        for (int i = 0; i < CONSTRAINTS; i++) {
            columns[i] = new ColumnNode(Integer.toString(i));
            prev.right = columns[i];
            columns[i].left = prev;
            prev = columns[i];
        }
        prev.right = header;
        header.left = prev;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int given = grid[r][c];
                for (int d = 1; d <= N; d++) {
                    if (given != 0 && given != d) continue;
                    addRow(columns, r, c, d);
                }
            }
        }
    }

    private void addRow(ColumnNode[] columns, int r, int c, int d) {
        int rowId = r * N * N + c * N + (d - 1);
        int box = (r / B) * B + (c / B);

        int cellCol = r * N + c;                       // 0..80
        int rowCol = N * N + r * N + (d - 1);          // 81..161
        int colCol = 2 * N * N + c * N + (d - 1);      // 162..242
        int boxCol = 3 * N * N + box * N + (d - 1);    // 243..323

        Node a = addNode(columns[cellCol], rowId);
        Node b = addNode(columns[rowCol], rowId);
        Node cN = addNode(columns[colCol], rowId);
        Node dN = addNode(columns[boxCol], rowId);

        a.right = b; b.left = a;
        b.right = cN; cN.left = b;
        cN.right = dN; dN.left = cN;
        dN.right = a; a.left = dN;
    }

    private Node addNode(ColumnNode col, int rowId) {
        Node node = new Node();
        node.column = col;
        node.rowId = rowId;
        node.up = col.up;
        node.down = col;
        col.up.down = node;
        col.up = node;
        col.size++;
        return node;
    }

    private int[][] toGrid(int[] flat) {
        int[][] out = new int[N][N];
        for (int i = 0; i < flat.length; i++) out[i / N][i % N] = flat[i];
        return out;
    }

    private static class Node {
        Node left, right, up, down;
        ColumnNode column;
        int rowId;

        Node() {
            left = right = up = down = this;
        }
    }

    private static final class ColumnNode extends Node {
        final String name;
        int size;

        ColumnNode(String name) {
            super();
            this.name = name;
            this.column = this;
        }
    }
}
