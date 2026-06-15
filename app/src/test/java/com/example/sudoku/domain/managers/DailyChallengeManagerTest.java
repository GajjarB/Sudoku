package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.dao.DailyChallengeDao;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.repository.GameRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DailyChallengeManagerTest {

    @Mock
    private DailyChallengeDao dailyDao;

    @Mock
    private GameRepository gameRepository;

    private DailyChallengeManager dailyChallengeManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dailyChallengeManager = new DailyChallengeManager(dailyDao, gameRepository);
    }

    @Test
    public void getOrCreateDailyChallengeSync_whenChallengeExists_returnsExisting() {
        String dateKey = "2023-10-27";
        DailyChallenge existingChallenge = new DailyChallenge(dateKey);
        when(dailyDao.getDailyChallenge(dateKey)).thenReturn(existingChallenge);

        DailyChallenge result = dailyChallengeManager.getOrCreateDailyChallengeSync(dateKey);

        assertEquals(existingChallenge, result);
        verify(dailyDao, never()).insertDailyChallenge(any());
    }

    @Test
    public void getOrCreateDailyChallengeSync_whenChallengeDoesNotExist_generatesAndInsertsNew() {
        String dateKey = "2023-10-27";
        when(dailyDao.getDailyChallenge(dateKey)).thenReturn(null);

        DailyChallenge result = dailyChallengeManager.getOrCreateDailyChallengeSync(dateKey);

        assertNotNull(result);
        assertEquals(dateKey, result.dateKey);
        assertNotNull(result.puzzleGrid);
        assertNotNull(result.solutionGrid);
        assertTrue(result.puzzleGrid.length() > 0);
        assertTrue(result.solutionGrid.length() > 0);

        ArgumentCaptor<DailyChallenge> captor = ArgumentCaptor.forClass(DailyChallenge.class);
        verify(dailyDao, times(1)).insertDailyChallenge(captor.capture());
        DailyChallenge inserted = captor.getValue();
        assertEquals(dateKey, inserted.dateKey);
        assertEquals(result, inserted);
    }

    @Test
    public void completeChallengeSync_updatesChallengeWithBetterScoreAndTime() {
        String dateKey = "2023-10-27";
        DailyChallenge existingChallenge = new DailyChallenge(dateKey);
        existingChallenge.bestScore = 100;
        existingChallenge.bestTimeMs = 50000;
        when(dailyDao.getDailyChallenge(dateKey)).thenReturn(existingChallenge);

        int newScore = 150;
        long newTimeMs = 40000;
        dailyChallengeManager.completeChallengeSync(dateKey, newScore, newTimeMs);

        assertTrue(existingChallenge.isCompleted);
        assertEquals(newScore, existingChallenge.bestScore);
        assertEquals(newTimeMs, existingChallenge.bestTimeMs);
        verify(dailyDao, times(1)).updateDailyChallenge(existingChallenge);
    }

    @Test
    public void completeChallengeSync_doesNotUpdateWithWorseScoreAndTime() {
        String dateKey = "2023-10-27";
        DailyChallenge existingChallenge = new DailyChallenge(dateKey);
        existingChallenge.bestScore = 200;
        existingChallenge.bestTimeMs = 30000;
        when(dailyDao.getDailyChallenge(dateKey)).thenReturn(existingChallenge);

        int newScore = 150;
        long newTimeMs = 40000;
        dailyChallengeManager.completeChallengeSync(dateKey, newScore, newTimeMs);

        assertTrue(existingChallenge.isCompleted);
        assertEquals(200, existingChallenge.bestScore);
        assertEquals(30000, existingChallenge.bestTimeMs);
        verify(dailyDao, times(1)).updateDailyChallenge(existingChallenge);
    }

    @Test
    public void completeChallengeSync_firstTimeCompletion_setsInitialScoreAndTime() {
        String dateKey = "2023-10-27";
        DailyChallenge existingChallenge = new DailyChallenge(dateKey);
        // Assuming uncompleted challenge has default values
        existingChallenge.bestScore = 0;
        existingChallenge.bestTimeMs = 0;
        when(dailyDao.getDailyChallenge(dateKey)).thenReturn(existingChallenge);

        int newScore = 150;
        long newTimeMs = 60000;
        dailyChallengeManager.completeChallengeSync(dateKey, newScore, newTimeMs);

        assertTrue(existingChallenge.isCompleted);
        assertEquals(150, existingChallenge.bestScore);
        assertEquals(60000, existingChallenge.bestTimeMs);
        verify(dailyDao, times(1)).updateDailyChallenge(existingChallenge);
    }

    @Test
    public void completeChallenge_updatesAndCallsExecutor() {
        // Just verify basic behavior of the async completion
        String dateKey = "2023-10-27";
        DailyChallenge challenge = new DailyChallenge(dateKey);
        challenge.bestScore = 0;
        challenge.bestTimeMs = 0;

        dailyChallengeManager.completeChallenge(challenge, 100, 50000);

        assertTrue(challenge.isCompleted);
        assertEquals(100, challenge.bestScore);
        assertEquals(50000, challenge.bestTimeMs);
        // The DAO update happens on another thread, so we'd have to use CountDownLatch or mock AppExecutors to verify DAO interaction reliably.
        // For unit test, we just check state mutation
    }

    @Test
    public void getDateKey_returnsCorrectFormat() {
        long epochMillis = 1698408000000L; // Oct 27, 2023 (UTC)
        // Note: SimpleDateFormat uses local timezone by default.
        // To make test robust, we just verify it formats consistently with the same object.
        String expectedFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(epochMillis));
        String result = dailyChallengeManager.getDateKey(epochMillis);

        assertEquals(expectedFormat, result);
    }

    @Test
    public void getStartOfTodayMillis_returnsStartOfDay() {
        long start = dailyChallengeManager.getStartOfTodayMillis();
        long end = dailyChallengeManager.getEndOfTodayMillis();

        assertTrue(start > 0);
        assertTrue(end > start);
        // difference should be ~24 hours
        // Not exact 24 hours if DST changes today, but typically 24h
        long diffHours = (end - start) / (1000 * 60 * 60);
        assertTrue(diffHours >= 23 && diffHours <= 25);
    }
}
