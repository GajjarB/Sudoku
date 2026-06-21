package com.example.sudoku.domain.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;

import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.CognitiveMetrics;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

public class ProgressiveDifficultySystemTest {

    private GameDao dao;
    private BehavioralAnalyzer analyzer;
    private IntelligentPuzzleGenerator puzzleGen;
    private ProgressiveDifficultySystem system;
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        mockedLog = Mockito.mockStatic(Log.class);
        mockedLog.when(() -> Log.d(Mockito.anyString(), Mockito.anyString())).thenReturn(0);

        dao = mock(GameDao.class);
        analyzer = mock(BehavioralAnalyzer.class);
        puzzleGen = mock(IntelligentPuzzleGenerator.class);

        system = new ProgressiveDifficultySystem(dao, analyzer, puzzleGen);
    }

    @After
    public void tearDown() {
        if (mockedLog != null) {
            mockedLog.close();
        }
    }

    @Test
    public void generateNextChallenge_playerStruggling_generatesEasierPuzzle() {
        CognitiveMetrics metrics = new CognitiveMetrics("2025-01-01");
        metrics.errorRate = Constants.AI_STRUGGLING_ERROR_RATE + 0.1;
        metrics.challengeReadiness = Constants.AI_STRUGGLING_READINESS - 5;
        metrics.improvementRate = -0.3;

        when(analyzer.analyzeCognitivePatterns()).thenReturn(metrics);

        SudokuGenerator.Puzzle puzzle = system.generateNextChallenge(1);

        assertNotNull(puzzle);
        assertTrue("Expected between 38 and 44 but got " + puzzle.clueCount, puzzle.clueCount >= 38 && puzzle.clueCount <= 44);
    }

    @Test
    public void generateNextChallenge_playerPlateauing_generatesBreakthroughPuzzle() {
        CognitiveMetrics metrics = new CognitiveMetrics("2025-01-01");
        metrics.errorRate = 0.2;
        metrics.plateauScore = Constants.AI_PLATEAU_THRESHOLD + 0.1;
        metrics.challengeReadiness = 45;

        when(analyzer.analyzeCognitivePatterns()).thenReturn(metrics);
        when(dao.getAllSolvingPatterns()).thenReturn(Collections.emptyList());

        String puzzleStr = "0".repeat(81);
        String solutionStr = "1".repeat(81);
        SudokuGenerator.Puzzle expectedPuzzle = new SudokuGenerator.Puzzle(puzzleStr, solutionStr, 30, 0);
        when(puzzleGen.generatePersonalizedPuzzle(1)).thenReturn(expectedPuzzle);

        SudokuGenerator.Puzzle puzzle = system.generateNextChallenge(1);

        assertEquals(expectedPuzzle, puzzle);
        verify(dao).getAllSolvingPatterns();
        verify(puzzleGen).generatePersonalizedPuzzle(1);
    }

    @Test
    public void generateNextChallenge_playerMastering_generatesHarderPuzzle() {
        CognitiveMetrics metrics = new CognitiveMetrics("2025-01-01");
        metrics.errorRate = Constants.AI_MASTERING_ERROR_RATE - 0.05;
        metrics.challengeReadiness = Constants.AI_MASTERING_READINESS + 5;
        metrics.avgDecisionTimeMs = Constants.AI_MASTERING_DECISION_TIME - 500;

        when(analyzer.analyzeCognitivePatterns()).thenReturn(metrics);

        SudokuGenerator.Puzzle puzzle = system.generateNextChallenge(1);

        assertNotNull(puzzle);
        assertTrue("Expected between 24 and 27 but got " + puzzle.clueCount, puzzle.clueCount >= 24 && puzzle.clueCount <= 27);
    }

    @Test
    public void generateNextChallenge_normalPlayer_generatesPersonalizedPuzzle() {
        CognitiveMetrics metrics = new CognitiveMetrics("2025-01-01");
        metrics.errorRate = 0.2;
        metrics.challengeReadiness = 50;
        metrics.plateauScore = 0.5;
        metrics.improvementRate = 0.1;

        when(analyzer.analyzeCognitivePatterns()).thenReturn(metrics);

        String puzzleStr = "0".repeat(81);
        String solutionStr = "1".repeat(81);
        SudokuGenerator.Puzzle expectedPuzzle = new SudokuGenerator.Puzzle(puzzleStr, solutionStr, 35, 0);
        when(puzzleGen.generatePersonalizedPuzzle(1)).thenReturn(expectedPuzzle);

        SudokuGenerator.Puzzle puzzle = system.generateNextChallenge(1);

        assertEquals(expectedPuzzle, puzzle);
        verify(puzzleGen).generatePersonalizedPuzzle(1);
    }
}
