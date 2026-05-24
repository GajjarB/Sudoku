package com.example.sudoku.domain.engine;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for SudokuGenerator: puzzle validity, uniqueness, difficulty ranges, seeded generation.
 */
public class SudokuGeneratorTest {

    private SudokuGenerator generator;
    private SudokuSolver solver;

    @Before
    public void setUp() {
        generator = new SudokuGenerator(42L); // Seeded for reproducibility
        solver = new SudokuSolver();
    }

    @Test
    public void generate_easyPuzzle_hasCorrectClueCount() {
        SudokuGenerator.Puzzle puzzle = generator.generate(38);
        int clues = countClues(puzzle.puzzle);
        assertEquals("Easy puzzle should have exact clue count", 38, clues);
    }

    @Test
    public void generate_hardPuzzle_hasFewerClues() {
        SudokuGenerator.Puzzle puzzle = generator.generate(26);
        int clues = countClues(puzzle.puzzle);
        assertEquals("Hard puzzle should have exact clue count", 26, clues);
    }

    @Test
    public void generate_puzzleHasUniqueSolution() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        int[][] grid = SudokuSolver.stringToGrid(puzzle.puzzle);
        assertTrue("Generated puzzle should have unique solution",
                solver.hasUniqueSolution(grid));
    }

    @Test
    public void generate_solutionIsValid() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        int[][] solution = SudokuSolver.stringToGrid(puzzle.solution);
        assertTrue("Solution should be a valid complete Sudoku", isValidCompleteSudoku(solution));
    }

    @Test
    public void generate_puzzleMatchesSolution() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        int[][] puzzleGrid = SudokuSolver.stringToGrid(puzzle.puzzle);
        int[][] solutionGrid = SudokuSolver.stringToGrid(puzzle.solution);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzleGrid[r][c] != 0) {
                    assertEquals("Clue at (" + r + "," + c + ") should match solution",
                            solutionGrid[r][c], puzzleGrid[r][c]);
                }
            }
        }
    }

    @Test
    public void generate_seededGenerator_reproducible() {
        SudokuGenerator gen1 = new SudokuGenerator(12345L);
        SudokuGenerator gen2 = new SudokuGenerator(12345L);

        SudokuGenerator.Puzzle p1 = gen1.generate(30);
        SudokuGenerator.Puzzle p2 = gen2.generate(30);

        assertEquals("Same seed should produce same solution", p1.solution, p2.solution);
    }

    @Test
    public void generate_puzzleStringsAre81Chars() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        assertEquals(81, puzzle.puzzle.length());
        assertEquals(81, puzzle.solution.length());
    }

    @Test
    public void generate_puzzleContainsOnlyValidChars() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        for (char c : puzzle.puzzle.toCharArray()) {
            assertTrue("Char '" + c + "' should be 0-9", c >= '0' && c <= '9');
        }
    }

    @Test
    public void generate_solutionContainsNoZeros() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        assertFalse("Solution should have no zeros", puzzle.solution.contains("0"));
    }

    @Test
    public void generate_defaultConstructor_works() {
        SudokuGenerator unseeded = new SudokuGenerator();
        SudokuGenerator.Puzzle puzzle = unseeded.generate(30);
        assertNotNull(puzzle);
        assertEquals(81, puzzle.puzzle.length());
    }

    @Test
    public void generate_invalidClueCount_throws() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(16));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(82));
    }

    @Test
    public void generate_requestedUnsolvedPuzzle_doesNotReturnSolvedBoardFallback() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        assertNotEquals("Puzzle should not be returned as a solved board",
                puzzle.solution, puzzle.puzzle);
        assertTrue("Puzzle should contain empty cells", puzzle.puzzle.contains("0"));
    }

    @Test
    public void generate_puzzleHasRotationalSymmetry() {
        SudokuGenerator.Puzzle puzzle = generator.generate(30);
        for (int i = 0; i < 81; i++) {
            boolean empty = puzzle.puzzle.charAt(i) == '0';
            boolean mirrorEmpty = puzzle.puzzle.charAt(80 - i) == '0';
            assertEquals("Cell " + i + " should mirror cell " + (80 - i),
                    empty, mirrorEmpty);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private int countClues(String puzzle) {
        int count = 0;
        for (char c : puzzle.toCharArray()) {
            if (c != '0') count++;
        }
        return count;
    }

    private boolean isValidCompleteSudoku(int[][] grid) {
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int val = grid[r][c];
                if (val < 1 || val > 9 || seen[val]) return false;
                seen[val] = true;
            }
        }
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                if (seen[grid[r][c]]) return false;
                seen[grid[r][c]] = true;
            }
        }
        return true;
    }
}
