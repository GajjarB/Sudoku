package com.example.sudoku.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.LevelProgress;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;
import com.example.sudoku.utils.Constants;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    private final SudokuDatabase database;

    private final LiveData<UserProfile> userProfile;
    private final LiveData<SavedGame> incompleteGame;
    private final MutableLiveData<List<LevelProgress>> levelPath = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        database = SudokuDatabase.getInstance(application);
        userRepository = new UserRepository(database.userProfileDao());
        gameRepository = new GameRepository(database.gameDao());
        levelRepository = new LevelRepository(database.levelProgressDao());

        userProfile = userRepository.getUserProfile();
        incompleteGame = gameRepository.getIncompleteGame();

        userRepository.ensureProfileExists("default_user");
        levelRepository.initializeLevels();
        refreshLevelPath();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<SavedGame> getIncompleteGame() {
        return incompleteGame;
    }

    public LiveData<List<LevelProgress>> getLevelPath() {
        return levelPath;
    }

    public void refreshLevelPath() {
        AppExecutors.getInstance().io().execute(() -> {
            levelRepository.ensureLevelsInitializedSync();
            int highestUnlocked = Math.max(1, levelRepository.getHighestUnlockedLevelSync());
            int limit = Math.min(Constants.MAX_LEVEL, Math.max(8, highestUnlocked + 3));
            levelPath.postValue(database.levelProgressDao().getLevels(limit));
        });
    }
}
