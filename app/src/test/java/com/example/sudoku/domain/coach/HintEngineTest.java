package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.utils.Constants;
import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;

public class HintEngineTest {

    @Test
    public void hintEngine_levelOne_returnsDirectionOnly() {
        HintEngine engine = new HintEngine();
        HintResult result = engine.getHint(oneEmptyGrid(), solvedGrid(), 1);

        assertTrue(result.available);
        assertFalse(result.applied);
        assertEquals(1, result.hintLevel);
        assertFalse(result.shortText.contains("Place"));
    }

    @Test
    public void hintEngine_levelFour_returnsFinalAnswer() {
        HintEngine engine = new HintEngine();
        HintResult result = engine.getHint(oneEmptyGrid(), solvedGrid(), 4);

        assertTrue(result.available);
        assertTrue(result.applied);
        assertEquals(1, result.value);
        assertEquals(0, result.targetRow);
        assertEquals(0, result.targetCol);
    }

    @Test
    public void nakedSingleDetector_findsSingleCandidate() {
        StrategyResult result = new NakedSingleDetector().detect(oneEmptyGrid(), solvedGrid());

        assertTrue(result.found);
        assertEquals(Constants.STRATEGY_NAKED_SINGLE, result.strategyName);
        assertEquals(1, result.value);
    }

    @Test
    public void hiddenSingleDetector_findsOnlyPlaceInRow() {
        int[][] grid = new int[9][9];
        grid[0][1] = 2;
        grid[0][2] = 3;
        grid[0][3] = 4;
        grid[0][4] = 5;
        grid[0][5] = 6;
        grid[0][6] = 7;
        grid[0][7] = 8;
        grid[0][8] = 9;

        StrategyResult result = new HiddenSingleDetector().detect(grid, solvedGrid());

        assertTrue(result.found);
        assertEquals(Constants.STRATEGY_HIDDEN_SINGLE, result.strategyName);
        assertEquals(1, result.value);
    }

    @Test
    public void strategyDetector_solvedGrid_returnsNone() {
        StrategyResult result = new StrategyDetector().detectNextMove(solvedGrid(), solvedGrid());

        assertFalse(result.found);
    }

    @Test
    public void hintEngine_eliminationStrategy_isAvailableButNotAppliedAsPlacement() {
        StrategyDetector detector = new StrategyDetector(
                Collections.emptyList(),
                Collections.singletonList(candidates -> CandidateElimination.StrategyHit.hit(
                        Constants.STRATEGY_POINTING_PAIR,
                        Collections.singletonList(new CandidateElimination(0, 1, 3)),
                        Collections.singletonList(new CellPosition(0, 0)),
                        "Pointing pair removes candidate 3.")));
        HintResult result = new HintEngine(detector).getHint(new int[9][9], null, 4);

        assertTrue(result.available);
        assertFalse(result.applied);
        assertEquals(Constants.STRATEGY_POINTING_PAIR, result.strategyName);
        assertTrue(result.shortText.contains("Remove 3"));
    }

    private int[][] oneEmptyGrid() {
        int[][] grid = solvedGrid();
        grid[0][0] = 0;
        return grid;
    }

    private int[][] solvedGrid() {
        return GameValidator.stringToGrid(
                "123456789456789123789123456214365897365897214897214365531642978642978531978531642");
    }
}
