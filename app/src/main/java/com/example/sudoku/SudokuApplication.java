package com.example.sudoku;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.workers.DailyAnalysisWorker;
import com.example.sudoku.workers.PuzzlePoolWorker;
import com.example.sudoku.workers.StreakCheckWorker;
import com.example.sudoku.utils.Constants;
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
                .addTag(Constants.WORKER_DAILY_ANALYSIS)
                .build();
                
        // Streak Check (runs daily to update streaks)
        PeriodicWorkRequest streakRequest = new PeriodicWorkRequest.Builder(
                StreakCheckWorker.class, 24, TimeUnit.HOURS)
                .addTag(Constants.WORKER_STREAK_CHECK)
                .build();
                
        workManager.enqueueUniquePeriodicWork(
                Constants.WORKER_DAILY_ANALYSIS,
                ExistingPeriodicWorkPolicy.KEEP,
                analysisRequest);
        workManager.enqueueUniquePeriodicWork(
                Constants.WORKER_STREAK_CHECK,
                ExistingPeriodicWorkPolicy.KEEP,
                streakRequest);

        // Kick off an immediate puzzle pool fill, plus a periodic top-up.
        Constraints poolConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        OneTimeWorkRequest poolKickoff = new OneTimeWorkRequest.Builder(PuzzlePoolWorker.class)
                .setConstraints(poolConstraints)
                .build();
        workManager.enqueueUniqueWork(
                "puzzle-pool-kickoff", ExistingWorkPolicy.KEEP, poolKickoff);

        PeriodicWorkRequest poolTopUp = new PeriodicWorkRequest.Builder(
                PuzzlePoolWorker.class, 6, TimeUnit.HOURS)
                .setConstraints(poolConstraints)
                .build();
        workManager.enqueueUniquePeriodicWork(
                "puzzle-pool-topup", ExistingPeriodicWorkPolicy.KEEP, poolTopUp);
    }

    @Override
    public void onTerminate() {
        AppExecutors.getInstance().shutdown();
        super.onTerminate();
    }
}
