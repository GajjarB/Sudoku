package com.example.sudoku.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.domain.ai.BehavioralAnalyzer;

/**
 * Worker for running daily AI analysis.
 */
public class DailyAnalysisWorker extends Worker {

    public DailyAnalysisWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SudokuDatabase db = SudokuDatabase.getInstance(getApplicationContext());
            GameRepository repo = new GameRepository(db.gameDao());
            BehavioralAnalyzer analyzer = new BehavioralAnalyzer(db.gameDao());
            
            // Run analysis
            analyzer.analyzeCognitivePatterns();
            
            // Allow repository to cleanup old data
            repo.cleanupOldData();
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
