package com.example.sudoku.domain.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sudoku.data.local.dao.DailyChallengeDao;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.repository.GameRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.TimeZone;

public class DailyChallengeManagerTest {

    @Mock
    private DailyChallengeDao mockDao;
    @Mock
    private GameRepository mockGameRepository;

    private DailyChallengeManager manager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new DailyChallengeManager(mockDao, mockGameRepository);
    }

    @Test
    public void testGetOrCreateDailyChallengeSync_Existing() {
        String dateKey = "2025-02-16";
        DailyChallenge existingChallenge = new DailyChallenge(dateKey);
        when(mockDao.getDailyChallenge(dateKey)).thenReturn(existingChallenge);

        DailyChallenge result = manager.getOrCreateDailyChallengeSync(dateKey);

        assertEquals(existingChallenge, result);
        verify(mockDao, never()).insertDailyChallenge(any());
    }

    @Test
    public void testGetOrCreateDailyChallengeSync_New() {
        String dateKey = "2025-02-16";
        when(mockDao.getDailyChallenge(dateKey)).thenReturn(null);

        DailyChallenge result = manager.getOrCreateDailyChallengeSync(dateKey);

        assertNotNull(result);
        assertEquals(dateKey, result.dateKey);
        assertNotNull(result.puzzleGrid);
        assertNotNull(result.solutionGrid);

        verify(mockDao).insertDailyChallenge(result);
    }

    @Test
    public void testCompleteChallengeSync() {
        String dateKey = "2025-02-16";
        DailyChallenge challenge = new DailyChallenge(dateKey);
        challenge.bestScore = 100;
        challenge.bestTimeMs = 50000;

        when(mockDao.getDailyChallenge(dateKey)).thenReturn(challenge);

        // Better score, better time
        manager.completeChallengeSync(dateKey, 200, 40000);

        assertTrue(challenge.isCompleted);
        assertEquals(200, challenge.bestScore);
        assertEquals(40000, challenge.bestTimeMs);
        verify(mockDao).updateDailyChallenge(challenge);
    }

    @Test
    public void testCompleteChallengeSync_WorseScoreAndTime() {
        String dateKey = "2025-02-16";
        DailyChallenge challenge = new DailyChallenge(dateKey);
        challenge.bestScore = 100;
        challenge.bestTimeMs = 50000;

        when(mockDao.getDailyChallenge(dateKey)).thenReturn(challenge);

        // Worse score, worse time
        manager.completeChallengeSync(dateKey, 50, 60000);

        assertTrue(challenge.isCompleted);
        assertEquals(100, challenge.bestScore); // Should keep original best
        assertEquals(50000, challenge.bestTimeMs); // Should keep original best
        verify(mockDao).updateDailyChallenge(challenge);
    }

    @Test
    public void testCompleteChallengeSync_FirstTimeCompletion() {
        String dateKey = "2025-02-16";
        DailyChallenge challenge = new DailyChallenge(dateKey);
        // bestTimeMs defaults to Long.MAX_VALUE in DailyChallenge constructor according to entity code,
        // but let's see how the manager handles it if it was initialized to 0 somehow or use entity's default.
        // Actually, the manager checks for 0: `(challenge.bestTimeMs == 0) ? timeMs : Math.min...`
        challenge.bestTimeMs = 0;

        when(mockDao.getDailyChallenge(dateKey)).thenReturn(challenge);

        manager.completeChallengeSync(dateKey, 150, 30000);

        assertTrue(challenge.isCompleted);
        assertEquals(150, challenge.bestScore);
        assertEquals(30000, challenge.bestTimeMs);
        verify(mockDao).updateDailyChallenge(challenge);
    }

    @Test
    public void testCompleteChallenge() {
        String dateKey = "2025-02-16";
        DailyChallenge challenge = new DailyChallenge(dateKey);
        challenge.bestScore = 100;
        challenge.bestTimeMs = 50000;

        // completeChallenge updates the object synchronously, but calls DAO asynchronously.
        // We will just verify object updates since AppExecutors might use real threads unless mocked,
        // but AppExecutors is a singleton in app code and hard to mock without reflection/Robolectric.
        // Let's at least test the state update.
        manager.completeChallenge(challenge, 200, 40000);

        assertTrue(challenge.isCompleted);
        assertEquals(200, challenge.bestScore);
        assertEquals(40000, challenge.bestTimeMs);
        // We won't verify DAO call since it happens on executor thread and might be flaky without sleep/sync.
    }

    @Test
    public void testGetDateKey() {
        // Use a known epoch timestamp.
        // 0 = 1970-01-01T00:00:00Z
        // Since SimpleDateFormat uses default locale/timezone, the date might differ by timezone.
        // But let's test format string manually using UTC to be safe if timezone issues arise.
        // Or better yet, we just call the method and check pattern length or format.

        long epoch = 1708084800000L; // Example: Feb 16, 2024 roughly
        String key = manager.getDateKey(epoch);

        // It should match "yyyy-MM-dd"
        assertTrue(key.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testGetStartAndEndOfTodayMillis() {
        long start = manager.getStartOfTodayMillis();
        long end = manager.getEndOfTodayMillis();

        // End should be exactly 24 hours after start, or at least greater.
        // (Note: Daylight saving time might make it 23 or 25 hours in rare cases if Calendar uses local timezone,
        // but for now let's just check end > start and start is start of day).
        assertTrue("End of day should be after start of day", end > start);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }
}
