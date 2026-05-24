package com.example.sudoku.data.repository;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sudoku.data.local.dao.UserProfileDao;
import com.example.sudoku.data.local.entities.UserProfile;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserRepositoryTest {

    @Mock
    private UserProfileDao userProfileDao;

    private AutoCloseable mocks;
    private ExecutorService executor;
    private UserRepository repository;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        executor = Executors.newSingleThreadExecutor();
        repository = new UserRepository(userProfileDao, executor);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdownNow();
        mocks.close();
    }

    @Test
    public void recordGameResultSync_createsMissingProfileAndStartsStreak() {
        long playedAt = toEpochMillis(LocalDate.of(2026, 5, 21));
        when(userProfileDao.getUserProfile()).thenReturn(null, new UserProfile("default_user"));

        repository.recordGameResultSync("default_user", true, 3, 0,
                10_000L, 100, 2, playedAt);

        verify(userProfileDao).insertUserProfile(org.mockito.ArgumentMatchers.any(UserProfile.class));
        verify(userProfileDao).recordGameResult(eq("default_user"), eq(1), eq(0), eq(3),
                eq(0), eq(10_000L), eq(100), eq(2), eq(playedAt));
        verify(userProfileDao).updateStreak("default_user", 1, 1);
    }

    @Test
    public void recordGameResultSync_consecutiveDayExtendsStreak() {
        UserProfile profile = new UserProfile("default_user");
        profile.currentStreak = 4;
        profile.longestStreak = 4;
        profile.lastPlayedDate = toEpochMillis(LocalDate.of(2026, 5, 20));
        long playedAt = toEpochMillis(LocalDate.of(2026, 5, 21));
        when(userProfileDao.getUserProfile()).thenReturn(profile);

        repository.recordGameResultSync("default_user", true, 2, 1,
                20_000L, 80, 5, playedAt);

        verify(userProfileDao).updateStreak("default_user", 5, 5);
    }

    private long toEpochMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
