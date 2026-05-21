package com.example.sudoku.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared app-wide executors. Repositories and ViewModels should not create
 * their own thread pools.
 */
public final class AppExecutors {
    private static volatile AppExecutors instance;

    private final ExecutorService io;

    private AppExecutors() {
        int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors());
        io = Executors.newFixedThreadPool(threadCount, new NamedThreadFactory("sudoku-io"));
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    public ExecutorService io() {
        return io;
    }

    public void shutdown() {
        io.shutdown();
    }

    public boolean isShutdown() {
        return io.isShutdown();
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger count = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, prefix + "-" + count.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        }
    }
}
