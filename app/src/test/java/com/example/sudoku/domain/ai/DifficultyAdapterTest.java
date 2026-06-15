package com.example.sudoku.domain.ai;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.utils.Constants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DifficultyAdapterTest {

    @Mock
    private GameDao gameDao;

    private DifficultyAdapter difficultyAdapter;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        difficultyAdapter = new DifficultyAdapter(gameDao);
    }

    @Test
    public void calculateOptimalClueCount_emptyHistory_returnsEasy() {
        when(gameDao.getRecentGames(anyInt())).thenReturn(Collections.emptyList());

        int clueCount = difficultyAdapter.calculateOptimalClueCount(1);

        assertTrue(clueCount == Constants.CLUES_EASY_MIN);
    }

    @Test
    public void calculateOptimalClueCount_highSkill_returnsExpert() {
        List<GameHistory> games = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GameHistory history = new GameHistory();
            history.accuracyRate = 1.0; // 100
            history.totalTimeMs = 2 * 60 * 1000; // < 3 mins -> 50
            history.starsEarned = 3; // -> 50
            // total = 200 -> expert
            games.add(history);
        }
        when(gameDao.getRecentGames(anyInt())).thenReturn(games);

        int clueCount = difficultyAdapter.calculateOptimalClueCount(1);

        assertTrue(clueCount >= Constants.CLUES_EXPERT_MIN && clueCount <= Constants.CLUES_EXPERT_MAX);
    }

    @Test
    public void calculateOptimalClueCount_mediumSkill_returnsMedium() {
        List<GameHistory> games = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GameHistory history = new GameHistory();
            history.accuracyRate = 0.5; // 50
            history.totalTimeMs = 4 * 60 * 1000; // < 6 mins -> 25
            history.starsEarned = 2; // -> 30
            // total = 105 -> medium (80-119)
            games.add(history);
        }
        when(gameDao.getRecentGames(anyInt())).thenReturn(games);

        int clueCount = difficultyAdapter.calculateOptimalClueCount(1);

        assertTrue(clueCount >= Constants.CLUES_MEDIUM_MIN && clueCount <= Constants.CLUES_MEDIUM_MAX);
    }

    @Test
    public void calculateOptimalClueCount_lowSkill_returnsEasy() {
        List<GameHistory> games = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GameHistory history = new GameHistory();
            history.accuracyRate = 0.2; // 20
            history.totalTimeMs = 10 * 60 * 1000; // > 6 mins -> 0
            history.starsEarned = 1; // -> 10
            // total = 30 -> easy (0-79)
            games.add(history);
        }
        when(gameDao.getRecentGames(anyInt())).thenReturn(games);

        int clueCount = difficultyAdapter.calculateOptimalClueCount(1);

        assertTrue(clueCount >= Constants.CLUES_EASY_MIN && clueCount <= Constants.CLUES_EASY_MAX);
    }

    @Test
    public void calculateOptimalClueCount_hardSkill_returnsHard() {
        List<GameHistory> games = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GameHistory history = new GameHistory();
            history.accuracyRate = 0.8; // 80
            history.totalTimeMs = 4 * 60 * 1000; // < 6 mins -> 25
            history.starsEarned = 2; // -> 30
            // total = 135 -> hard (120-159)
            games.add(history);
        }
        when(gameDao.getRecentGames(anyInt())).thenReturn(games);

        int clueCount = difficultyAdapter.calculateOptimalClueCount(1);

        assertTrue(clueCount >= Constants.CLUES_HARD_MIN && clueCount <= Constants.CLUES_HARD_MAX);
    }
}
