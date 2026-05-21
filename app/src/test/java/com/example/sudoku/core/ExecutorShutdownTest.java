package com.example.sudoku.core;

import com.example.sudoku.data.repository.GameRepository;
import org.junit.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class ExecutorShutdownTest {

    @Test
    public void repositoryShutdown_doesNotShutdownInjectedSharedExecutor() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            GameRepository repository = new GameRepository(null, executor);

            repository.shutdown();

            assertFalse("Repository must not own shared executor shutdown", executor.isShutdown());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        }
    }

    @Test
    public void appExecutors_ioUsesNamedSharedThreads() throws Exception {
        final String[] threadName = new String[1];

        AppExecutors.getInstance().io().submit(() -> threadName[0] = Thread.currentThread().getName()).get();

        assertNotNull(threadName[0]);
        assertTrue(threadName[0].startsWith("sudoku-io-"));
    }
}
