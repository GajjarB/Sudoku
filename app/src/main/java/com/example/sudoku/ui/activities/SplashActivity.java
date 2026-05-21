package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
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

        findViewById(R.id.btn_continue).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.btn_sign_in).setOnClickListener(v ->
                Toast.makeText(this, "Sign in is optional for local play.", Toast.LENGTH_SHORT).show());
    }
}
