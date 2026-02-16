package com.example.sudoku.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.data.repository.UserRepository;

/**
 * ViewModel for the Home/Main Activity.
 */
public class HomeViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    
    private final LiveData<UserProfile> userProfile;
    private final LiveData<SavedGame> incompleteGame;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        SudokuDatabase db = SudokuDatabase.getInstance(application);
        userRepository = new UserRepository(db.userProfileDao());
        gameRepository = new GameRepository(db.gameDao());
        
        userProfile = userRepository.getUserProfile();
        incompleteGame = gameRepository.getIncompleteGame();
        
        // Ensure profile exists for "default_user"
        userRepository.ensureProfileExists("default_user");
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    public LiveData<SavedGame> getIncompleteGame() {
        return incompleteGame;
    }
}
