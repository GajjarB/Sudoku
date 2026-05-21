package com.example.sudoku.core;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Observes process-wide foreground/background transitions so the app can
 * flush in-memory game state before the OS reclaims the process.
 */
public class AppLifecycleObserver implements DefaultLifecycleObserver {

    public interface Listener {
        void onAppBackgrounded();
        void onAppForegrounded();
    }

    private final Listener listener;

    public AppLifecycleObserver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        if (listener != null) listener.onAppBackgrounded();
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        if (listener != null) listener.onAppForegrounded();
    }
}
