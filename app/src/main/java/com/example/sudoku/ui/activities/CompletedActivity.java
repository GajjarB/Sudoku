package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import com.example.sudoku.R;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.utils.Constants;
import java.util.Locale;

public class CompletedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);

        long timeMs = getIntent().getLongExtra(Constants.EXTRA_COMPLETION_TIME, 0L);
        int mistakes = getIntent().getIntExtra(Constants.EXTRA_COMPLETION_MISTAKES, 0);
        int level = getIntent().getIntExtra(Constants.EXTRA_COMPLETION_LEVEL, 1);
        int hintsUsed = getIntent().getIntExtra("extra_completion_hints_used", 0);
        String difficulty = getIntent().getStringExtra("extra_completion_difficulty");
        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = getString(R.string.difficulty_easy);
        }
        int accuracy = Math.max(0, 100 - mistakes * 12);
        int xpEarned = Constants.BASE_XP;

        ((TextView) findViewById(R.id.tv_completed_time)).setText(formatTime(timeMs) + "\n" + getString(R.string.completed_time_label));
        ((TextView) findViewById(R.id.tv_completed_mistakes)).setText(mistakes + "\n" + getString(R.string.completed_mistakes_label));
        ((TextView) findViewById(R.id.tv_completed_accuracy)).setText(accuracy + "%\n" + getString(R.string.completed_accuracy_label));
        ((TextView) findViewById(R.id.tv_completed_hints)).setText(hintsUsed + "\n" + getString(R.string.completed_hints_label));
        ((TextView) findViewById(R.id.tv_completed_difficulty)).setText(difficulty + "\n" + getString(R.string.completed_difficulty_label));
        ((TextView) findViewById(R.id.tv_completed_xp)).setText("+" + xpEarned + "\n" + getString(R.string.completed_xp_label));
        findViewById(R.id.tv_trophy).startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));

        Button btnPlayAgain = findViewById(R.id.btn_continue);
        Button btnNext = findViewById(R.id.btn_next_puzzle);
        Button btnHome = findViewById(R.id.btn_back_home);
        applyDuolingoPressEffect(btnPlayAgain, btnNext, btnHome);
        btnPlayAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, level);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, level + 1);
            startActivity(intent);
            finish();
        });
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        AppExecutors.getInstance().io().execute(() -> {
            com.example.sudoku.data.local.entities.UserProfile profile =
                    SudokuDatabase.getInstance(getApplicationContext()).userProfileDao().getUserProfile();
            runOnUiThread(() -> {
                TextView streakView = findViewById(R.id.tv_completed_streak);
                TextView goalProgressView = findViewById(R.id.tv_completed_goal_progress);
                int streak = profile != null ? profile.currentStreak : 0;
                streakView.setText("🔥 " + streak + " Day Streak");
                goalProgressView.setText("Level " + (level + 1)
                        + " unlocked. Daily goals and streak progress were updated after this win.");
            });
        });
    }

    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
