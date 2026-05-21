package com.example.sudoku.domain.engine;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cooperative cancellation flag. Long-running engine operations poll
 * {@link #isCancelled()} between work units and exit cleanly when set.
 */
public final class CancellationToken {

    public static final CancellationToken NONE = new CancellationToken();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }
}
