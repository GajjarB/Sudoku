package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sudoku.R;
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
        int accuracy = Math.max(0, 100 - mistakes * 12);

        ((TextView) findViewById(R.id.tv_completed_time)).setText(formatTime(timeMs) + "\nTime");
        ((TextView) findViewById(R.id.tv_completed_mistakes)).setText(mistakes + "\nMistakes");
        ((TextView) findViewById(R.id.tv_completed_accuracy)).setText(accuracy + "%\nAccuracy");
        ((TextView) findViewById(R.id.tv_completed_xp)).setText("+100\nXP");

        findViewById(R.id.btn_next_puzzle).setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, level + 1);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btn_share).setOnClickListener(v ->
                Toast.makeText(this, R.string.completed_share_toast, Toast.LENGTH_SHORT).show());
    }

    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
