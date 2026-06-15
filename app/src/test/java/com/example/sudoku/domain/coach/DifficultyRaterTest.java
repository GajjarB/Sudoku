package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.engine.Difficulty;
import com.example.sudoku.utils.Constants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DifficultyRaterTest {

    private DifficultyRater difficultyRater;

    @Mock
    private LogicalSolver mockLogicalSolver;

    private int[][] mockPuzzle = new int[9][9];
    private int[][] mockSolution = new int[9][9];

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        difficultyRater = new DifficultyRater();

        // Inject the mocked LogicalSolver using reflection
        Field solverField = DifficultyRater.class.getDeclaredField("logicalSolver");
        solverField.setAccessible(true);
        solverField.set(difficultyRater, mockLogicalSolver);
    }

    // Helper method to create LogicalSolver.Result since constructor is private
    private LogicalSolver.Result createResult(boolean solved, int[][] grid, Map<String, Integer> usage, int steps) throws Exception {
        Constructor<LogicalSolver.Result> constructor = LogicalSolver.Result.class.getDeclaredConstructor(boolean.class, int[][].class, Map.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(solved, grid, usage, steps);
    }

    @Test
    public void testRate_UnsolvedPuzzle_ReturnsExtremeDifficulty() throws Exception {
        Map<String, Integer> usage = new HashMap<>();
        usage.put(Constants.STRATEGY_NAKED_SINGLE, 5);

        LogicalSolver.Result mockResult = createResult(false, mockPuzzle, usage, 5);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.EXTREME, rating.difficulty);
        assertEquals(Double.POSITIVE_INFINITY, rating.score, 0.0);
        assertEquals(usage, rating.strategyUsage);
    }

    @Test
    public void testRate_EasyPuzzle_ReturnsBeginnerOrEasy() throws Exception {
        Map<String, Integer> usage = new HashMap<>();
        usage.put(Constants.STRATEGY_NAKED_SINGLE, 20); // weight 1.0
        // maxWeight = 1.0, weightedSum = 20, totalSteps = 20
        // frequencyFactor = 20 / 20 = 1.0
        // score = 1.0 + 0.2 * 1.0 = 1.2
        // maxStrategyScore for BEGINNER is 1.0, EASY is 2.0. So 1.2 is EASY.

        LogicalSolver.Result mockResult = createResult(true, mockSolution, usage, 20);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.EASY, rating.difficulty);
        assertEquals(1.2, rating.score, 0.0001);
        assertEquals(usage, rating.strategyUsage);
    }

    @Test
    public void testRate_MediumPuzzle_ReturnsMedium() throws Exception {
        Map<String, Integer> usage = new HashMap<>();
        usage.put(Constants.STRATEGY_NAKED_SINGLE, 15); // weight 1.0
        usage.put(Constants.STRATEGY_NAKED_PAIR, 5); // weight 3.5
        // maxWeight = 3.5
        // weightedSum = (15 * 1.0) + (5 * 3.5) = 15 + 17.5 = 32.5
        // totalSteps = 20
        // frequencyFactor = 32.5 / 20 = 1.625
        // score = 3.5 + 0.2 * 1.625 = 3.5 + 0.325 = 3.825
        // maxStrategyScore for MEDIUM is 3.5, HARD is 5.0. 3.825 is HARD.

        LogicalSolver.Result mockResult = createResult(true, mockSolution, usage, 20);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.HARD, rating.difficulty);
        assertEquals(3.825, rating.score, 0.0001);
    }

    @Test
    public void testRate_HardPuzzle_ReturnsExpertOrMaster() throws Exception {
        Map<String, Integer> usage = new HashMap<>();
        usage.put(Constants.STRATEGY_NAKED_SINGLE, 30); // weight 1.0
        usage.put(Constants.STRATEGY_X_WING, 2); // weight 6.5
        // maxWeight = 6.5
        // weightedSum = 30 + 13 = 43
        // totalSteps = 32
        // frequencyFactor = 43 / 32 = 1.34375
        // score = 6.5 + 0.2 * 1.34375 = 6.5 + 0.26875 = 6.76875
        // MASTER maxStrategyScore is 8.0, EXPERT is 6.5.
        // 6.76875 is MASTER.

        LogicalSolver.Result mockResult = createResult(true, mockSolution, usage, 32);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.MASTER, rating.difficulty);
        assertEquals(6.76875, rating.score, 0.0001);
    }

    @Test
    public void testRate_EmptyUsage_ReturnsBeginner() throws Exception {
        // Edge case: Puzzle is already solved or requires 0 steps
        Map<String, Integer> usage = new HashMap<>();

        LogicalSolver.Result mockResult = createResult(true, mockSolution, usage, 0);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.BEGINNER, rating.difficulty);
        assertEquals(0.0, rating.score, 0.0001);
    }

    @Test
    public void testRate_UnknownStrategy_UsesDefaultWeight() throws Exception {
        Map<String, Integer> usage = new HashMap<>();
        usage.put("UNKNOWN_STRATEGY", 1); // default weight is 5.0

        // maxWeight = 5.0
        // totalSteps = 1
        // weightedSum = 5.0
        // frequencyFactor = 5.0
        // score = 5.0 + 0.2 * 5.0 = 6.0
        // maxStrategyScore for HARD is 5.0, EXPERT is 6.5.
        // 6.0 is EXPERT.

        LogicalSolver.Result mockResult = createResult(true, mockSolution, usage, 1);
        when(mockLogicalSolver.solve(any(int[][].class), any(int[][].class))).thenReturn(mockResult);

        DifficultyRater.Rating rating = difficultyRater.rate(mockPuzzle, mockSolution);

        assertEquals(Difficulty.EXPERT, rating.difficulty);
        assertEquals(6.0, rating.score, 0.0001);
    }
}
