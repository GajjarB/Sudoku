package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.R;
import com.example.sudoku.ui.utils.UiEffects;

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

        // Entrance animations
        findViewById(R.id.iv_logo).startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_up));
        findViewById(R.id.tv_welcome).startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in));

        UiEffects.applyDuolingoPressEffect(findViewById(R.id.btn_continue));
        findViewById(R.id.btn_continue).setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        });
    }
}
