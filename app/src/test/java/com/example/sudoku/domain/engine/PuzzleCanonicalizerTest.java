package com.example.sudoku.domain.engine;

import org.junit.Test;
import static org.junit.Assert.*;

public class PuzzleCanonicalizerTest {

    private static final String PUZZLE_A =
            "530070000" +
            "600195000" +
            "098000060" +
            "800060003" +
            "400803001" +
            "700020006" +
            "060000280" +
            "000419005" +
            "000080079";

    @Test
    public void canonicalForm_isStable() {
        String c1 = PuzzleCanonicalizer.canonicalForm(PUZZLE_A);
        String c2 = PuzzleCanonicalizer.canonicalForm(PUZZLE_A);
        assertEquals(c1, c2);
        assertEquals(81, c1.length());
    }

    @Test
    public void canonicalForm_digitRelabelingEquivalent() {
        // Swap digits 1<->2 across the puzzle — canonical form must match.
        StringBuilder swapped = new StringBuilder(PUZZLE_A.length());
        for (char ch : PUZZLE_A.toCharArray()) {
            if (ch == '1') swapped.append('2');
            else if (ch == '2') swapped.append('1');
            else swapped.append(ch);
        }
        assertEquals(
                PuzzleCanonicalizer.canonicalForm(PUZZLE_A),
                PuzzleCanonicalizer.canonicalForm(swapped.toString()));
    }

    @Test
    public void canonicalHash_differsForDifferentPuzzles() {
        String alt = PUZZLE_A.replaceFirst("5", "0");
        assertNotEquals(
                PuzzleCanonicalizer.canonicalHash(PUZZLE_A),
                PuzzleCanonicalizer.canonicalHash(alt));
    }

    @Test
    public void canonicalForm_rejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class,
                () -> PuzzleCanonicalizer.canonicalForm("123"));
    }
}
