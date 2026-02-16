package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudoku.R;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;
import com.example.sudoku.data.local.database.SudokuDatabase;

/**
 * Splash Screen Activity.
 * Initializes data and transitions to Main Activity.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize DB and default data
        SudokuDatabase db = SudokuDatabase.getInstance(getApplicationContext());
        UserRepository userRepo = new UserRepository(db.userProfileDao());
        LevelRepository levelRepo = new LevelRepository(db.levelProgressDao());
        
        userRepo.ensureProfileExists("default_user");
        levelRepo.initializeLevels();
        
        // Delay for branding and loading
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
