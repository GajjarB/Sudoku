package com.example.sudoku.ui.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import java.util.Locale;

/**
 * Custom TextView acting as a game timer.
 */
public class TimerView extends AppCompatTextView {
    
    private long startTime;
    private boolean isRunning;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                
                setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        }
    };

    public TimerView(Context context) {
        super(context);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            isRunning = true;
            handler.post(updateRunnable);
        }
    }
    
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(updateRunnable);
    }
    
    public void setTime(long elapsedMillis) {
        startTime = System.currentTimeMillis() - elapsedMillis;
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
