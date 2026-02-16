package com.example.sudoku;

import android.app.Application;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.workers.DailyAnalysisWorker;
import com.example.sudoku.workers.StreakCheckWorker;
import java.util.concurrent.TimeUnit;

public class SudokuApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Database
        SudokuDatabase.getInstance(this);
        
        // Schedule Workers
        scheduleDailyWorkers();
    }
    
    private void scheduleDailyWorkers() {
        WorkManager workManager = WorkManager.getInstance(this);
        
        // Daily Analysis (runs once a day, preferably at night)
        PeriodicWorkRequest analysisRequest = new PeriodicWorkRequest.Builder(
                DailyAnalysisWorker.class, 24, TimeUnit.HOURS)
                .build();
                
        // Streak Check (runs daily to update streaks)
        PeriodicWorkRequest streakRequest = new PeriodicWorkRequest.Builder(
                StreakCheckWorker.class, 24, TimeUnit.HOURS)
                .build();
                
        workManager.enqueue(analysisRequest);
        workManager.enqueue(streakRequest);
    }
}
