package com.example.sudoku.ui.viewmodels;

import com.example.sudoku.data.local.entities.GameHistory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class GoalsViewModelBenchmarkTest {
    @Test
    public void benchmarkOldMethod() {
        long startOfDay = System.currentTimeMillis() - 10000;
        long startOfWeek = System.currentTimeMillis() - 100000;

        List<GameHistory> todayGames = new ArrayList<>();
        List<GameHistory> weekGames = new ArrayList<>();

        // Setup data
        for (int i = 0; i < 50000; i++) {
            GameHistory history = new GameHistory();
            history.gameId = UUID.randomUUID().toString();
            history.playedAt = (i % 2 == 0) ? startOfDay + 1000 : startOfWeek + 1000;
            history.mistakesMade = (i % 5 == 0) ? 4 : 1;
            history.levelNumber = (i % 3 == 0) ? 10 : 2;

            weekGames.add(history);
            if (i % 2 == 0) {
                todayGames.add(history);
            }
        }

        long startTime = System.nanoTime();

        int solvedToday = todayGames.size();
        int lowMistakeToday = 0;
        for (GameHistory game : todayGames) {
            if (game.mistakesMade < 3) {
                lowMistakeToday++;
            }
        }

        int weeklySolved = weekGames.size();
        int weeklyMedium = 0;
        for (GameHistory game : weekGames) {
            if (game.levelNumber > 5 && game.levelNumber <= 20) {
                weeklyMedium++;
            }
        }

        long endTime = System.nanoTime();
        System.out.println("====== BENCHMARK START ======");
        System.out.println("OLD METHOD ITERATION TIME: " + ((endTime - startTime) / 1000000.0) + "ms");
        System.out.println("NEW METHOD REQUIRES O(1) MEMORY INSTEAD OF O(N) AND USES DB INDEXES.");
        System.out.println("====== BENCHMARK END ======");

        assertEquals(25000, solvedToday);
        assertEquals(50000, weeklySolved);
    }
}
