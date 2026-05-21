package com.example.sudoku.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.LevelProgress;
import com.example.sudoku.data.repository.LevelRepository;

import java.util.List;

/**
 * ViewModel for LevelSelectActivity.
 * Owns the LevelRepository lifecycle and exposes level data via LiveData.
 */
public class LevelSelectViewModel extends AndroidViewModel {

    private final LevelRepository levelRepository;
    private final LiveData<List<LevelProgress>> unlockedLevels;

    public LevelSelectViewModel(@NonNull Application application) {
        super(application);
        SudokuDatabase db = SudokuDatabase.getInstance(application);
        this.levelRepository = new LevelRepository(db.levelProgressDao());
        levelRepository.initializeLevels();
        this.unlockedLevels = levelRepository.getUnlockedLevels();
    }

    public LiveData<List<LevelProgress>> getUnlockedLevels() {
        return unlockedLevels;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        levelRepository.shutdown();
    }
}
