package com.example.sudoku.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sudoku.data.local.dao.LevelProgressDao;
import com.example.sudoku.utils.Constants;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LevelRepositoryTest {

    @Mock
    private LevelProgressDao levelDao;

    private AutoCloseable mocks;
    private ExecutorService executor;
    private LevelRepository repository;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        executor = Executors.newSingleThreadExecutor();
        repository = new LevelRepository(levelDao, executor);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdownNow();
        mocks.close();
    }

    @Test
    public void ensureLevelsInitializedSync_insertsMissingLevels() {
        when(levelDao.getLevelCount()).thenReturn(0);

        repository.ensureLevelsInitializedSync();

        verify(levelDao).insertAll(org.mockito.ArgumentMatchers.argThat(levels ->
                levels != null && levels.size() == Constants.MAX_LEVEL
                        && levels.get(0).levelNumber == 1
                        && levels.get(Constants.MAX_LEVEL - 1).levelNumber == Constants.MAX_LEVEL));
    }

    @Test
    public void ensureLevelsInitializedSync_skipsWhenComplete() {
        when(levelDao.getLevelCount()).thenReturn(Constants.MAX_LEVEL);

        repository.ensureLevelsInitializedSync();

        verify(levelDao, never()).insertAll(anyList());
    }

    @Test
    public void isLevelUnlockedSync_checksAfterInitialization() {
        when(levelDao.getLevelCount()).thenReturn(Constants.MAX_LEVEL);
        when(levelDao.isLevelUnlocked(1)).thenReturn(true);
        when(levelDao.isLevelUnlocked(25)).thenReturn(false);

        assertTrue(repository.isLevelUnlockedSync(1));
        assertFalse(repository.isLevelUnlockedSync(25));
    }
}
