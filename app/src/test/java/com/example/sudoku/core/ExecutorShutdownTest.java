package com.example.sudoku.core;

import com.example.sudoku.data.repository.GameRepository;
import org.junit.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class ExecutorShutdownTest {


    @Test
    public void appExecutors_ioUsesNamedSharedThreads() throws Exception {
        final String[] threadName = new String[1];

        AppExecutors.getInstance().io().submit(() -> threadName[0] = Thread.currentThread().getName()).get();

        assertNotNull(threadName[0]);
        assertTrue(threadName[0].startsWith("sudoku-io-"));
    }
}
