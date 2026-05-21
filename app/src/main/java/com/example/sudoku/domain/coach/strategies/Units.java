package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateGrid;

/**
 * Helpers for enumerating Sudoku units (row/column/box) as flat cell
 * indices (row * N + col).
 */
final class Units {

    static final int N = CandidateGrid.N;
    static final int B = CandidateGrid.B;

    private Units() {}

    static int[] row(int r) {
        int[] cells = new int[N];
        for (int c = 0; c < N; c++) cells[c] = r * N + c;
        return cells;
    }

    static int[] col(int c) {
        int[] cells = new int[N];
        for (int r = 0; r < N; r++) cells[r] = r * N + c;
        return cells;
    }

    static int[] box(int b) {
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
