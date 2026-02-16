package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sudoku.R;
import com.example.sudoku.ui.viewmodels.HomeViewModel;

/**
 * Main Activity (Home Screen).
 */
public class MainActivity extends BaseActivity {

    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        Button btnPlay = findViewById(R.id.btn_play);
        Button btnDaily = findViewById(R.id.btn_daily);
        Button btnSettings = findViewById(R.id.btn_settings);
        TextView tvLevel = findViewById(R.id.tv_player_level);
        
        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LevelSelectActivity.class));
        });
        
        btnDaily.setOnClickListener(v -> {
            // Check if processed or go directly to game with DAILY flag
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("IS_DAILY_CHALLENGE", true);
            startActivity(intent);
        });
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                tvLevel.setText("Level " + profile.currentLevel);
                // Update streak UI, etc.
            }
        });
        
        viewModel.getIncompleteGame().observe(this, game -> {
            Button btnResume = findViewById(R.id.btn_resume);
            if (game != null) {
                btnResume.setVisibility(View.VISIBLE);
                btnResume.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("GAME_ID", game.gameId);
                    startActivity(intent);
                });
            } else {
                btnResume.setVisibility(View.GONE);
            }
        });
    }
}
