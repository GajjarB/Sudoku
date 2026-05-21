package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import com.example.sudoku.R;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.preferences.PreferencesManager;
import com.example.sudoku.ui.viewmodels.HomeViewModel;
import com.example.sudoku.utils.Constants;

/**
 * Main Activity (Home Screen).
 */
public class MainActivity extends BaseActivity {

    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStoredThemeMode();
        setContentView(R.layout.activity_main);
        
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Splash passes RESUME_GAME_ID when an incomplete game exists; fast path
        // straight into the game so the player lands where they left off.
        String resumeId = getIntent().getStringExtra(Constants.EXTRA_RESUME_GAME);
        if (resumeId != null && savedInstanceState == null) {
            Intent resume = new Intent(MainActivity.this, GameActivity.class);
            resume.putExtra(Constants.EXTRA_GAME_ID, resumeId);
            startActivity(resume);
            finish();
            return;
        }
        
        Button btnPlay = findViewById(R.id.btn_play);
        Button btnDaily = findViewById(R.id.btn_daily);
        View btnSettings = findViewById(R.id.btn_settings);
        Button btnStats = findViewById(R.id.btn_stats);
        TextView continueTitle = findViewById(R.id.tv_continue_title);
        TextView tvLevel = findViewById(R.id.tv_player_level);
        TextView gamesPlayed = findViewById(R.id.tv_games_played);
        TextView winRate = findViewById(R.id.tv_win_rate);
        TextView bestTime = findViewById(R.id.tv_best_time);
        TextView streak = findViewById(R.id.tv_streak);
        TextView dailyDate = findViewById(R.id.tv_daily_date);
        TextView dailyStatus = findViewById(R.id.tv_daily_status);
        ProgressBar dailyProgress = findViewById(R.id.progress_daily);

        dailyDate.setText(new SimpleDateFormat("MMM d", Locale.US).format(new Date()));
        
        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LevelSelectActivity.class));
        });

        findViewById(R.id.btn_easy).setOnClickListener(v -> openDifficulty("Easy"));
        findViewById(R.id.btn_medium).setOnClickListener(v -> openDifficulty("Medium"));
        findViewById(R.id.btn_hard).setOnClickListener(v -> openDifficulty("Hard"));
        findViewById(R.id.btn_expert).setOnClickListener(v -> openDifficulty("Expert"));
        findViewById(R.id.btn_challenges).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChallengesActivity.class)));
        findViewById(R.id.btn_nav_challenges).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChallengesActivity.class)));
        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        findViewById(R.id.btn_themes).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ThemesActivity.class)));
        findViewById(R.id.btn_premium).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PremiumActivity.class)));
        
        btnDaily.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra(Constants.EXTRA_IS_DAILY_CHALLENGE, true);
            startActivity(intent);
        });
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        btnStats.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
        });
        
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                tvLevel.setText("Ready for Level " + profile.currentLevel);
                int played = profile.totalGamesPlayed;
                int wins = profile.totalGamesWon;
                int rate = played == 0 ? 0 : Math.round((wins * 100f) / played);
                gamesPlayed.setText(played + "\nGames");
                winRate.setText(rate + "%\nWins");
                bestTime.setText(formatTime(profile.totalPlayTimeMs) + "\nPlayed");
                streak.setText(profile.currentStreak + "\nStreak");
            }
        });
        
        viewModel.getIncompleteGame().observe(this, game -> {
        Button btnResume = findViewById(R.id.btn_resume);
            if (game != null) {
                continueTitle.setText("Continue Game");
                tvLevel.setText("Level " + game.levelNumber + " in progress");
                btnResume.setVisibility(View.VISIBLE);
                btnResume.setText("Resume");
                btnResume.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra(Constants.EXTRA_GAME_ID, game.gameId);
                    startActivity(intent);
                });
                updateDailyProgress(game, dailyProgress, dailyStatus, btnDaily);
            } else {
                continueTitle.setText("New Puzzle");
                btnResume.setVisibility(View.VISIBLE);
                btnResume.setText("Play");
                btnResume.setOnClickListener(v ->
                        startActivity(new Intent(MainActivity.this, LevelSelectActivity.class)));
                dailyProgress.setProgress(0);
                dailyStatus.setText("No daily progress yet. Finish one board today.");
                btnDaily.setText("Start Daily");
                btnDaily.setEnabled(true);
            }
        });
    }

    private void startLevel(int levelNumber) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, levelNumber);
        startActivity(intent);
    }

    private void openDifficulty(String difficulty) {
        Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "--";
        long minutes = millis / 60000;
        long hours = minutes / 60;
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }

    private void updateDailyProgress(SavedGame game, ProgressBar progress, TextView status, Button dailyButton) {
        if (!game.isDailyChallenge) {
            progress.setProgress(0);
            status.setText("No daily progress yet. Finish one board today.");
            dailyButton.setText("Start Daily");
            dailyButton.setEnabled(true);
            return;
        }

        int filled = countFilledCells(game.currentGrid);
        int percent = Math.round((filled * 100f) / 81f);
        progress.setProgress(percent);
        status.setText(game.isCompleted
                ? "Daily completed. Come back tomorrow for a fresh board."
                : percent + "% complete. Continue today's board.");
        dailyButton.setText(game.isCompleted ? "Daily Complete" : "Continue Daily");
        dailyButton.setEnabled(!game.isCompleted);
        if (!game.isCompleted) {
            dailyButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra(Constants.EXTRA_GAME_ID, game.gameId);
                startActivity(intent);
            });
        }
    }

    private int countFilledCells(String grid) {
        if (grid == null) return 0;
        int filled = 0;
        for (int i = 0; i < grid.length(); i++) {
            char value = grid.charAt(i);
            if (value >= '1' && value <= '9') filled++;
        }
        return filled;
    }

    private void applyStoredThemeMode() {
        String theme = new PreferencesManager(this).getTheme();
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("SYSTEM".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
