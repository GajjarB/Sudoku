package com.example.sudoku.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sudoku.R;

public class ProgressChartView extends LinearLayout {

    private String[] labels = new String[0];
    private int[] values = new int[0];

    public ProgressChartView(Context context) {
        super(context);
        init();
    }

    public ProgressChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.BOTTOM);
    }

    public void setData(String[] labels, int[] values) {
        this.labels = labels == null ? new String[0] : labels;
        this.values = values == null ? new int[0] : values;
        render();
    }

    private void render() {
        removeAllViews();
        int max = 1;
        for (int value : values) {
            max = Math.max(max, value);
        }
        for (int i = 0; i < Math.max(labels.length, values.length); i++) {
            int value = i < values.length ? values[i] : 0;
            String label = i < labels.length ? labels[i] : "";

            LinearLayout column = new LinearLayout(getContext());
            column.setOrientation(VERTICAL);
            column.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            column.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

            Space spacer = new Space(getContext());
            spacer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, Math.max(0.2f, max - value)));
            column.addView(spacer);

            View bar = new View(getContext());
            LayoutParams barParams = new LayoutParams(dp(18), Math.max(dp(18), dp(24 + (96 * value / max))));
            bar.setLayoutParams(barParams);
            bar.setBackgroundResource(R.drawable.bg_chart_bar);
            column.addView(bar);

            TextView valueView = new TextView(getContext());
            valueView.setText(String.valueOf(value));
            valueView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            valueView.setTextSize(11f);
            valueView.setTypeface(Typeface.DEFAULT_BOLD);
            valueView.setPadding(0, dp(8), 0, 0);
            column.addView(valueView);

            TextView labelView = new TextView(getContext());
            labelView.setText(label);
            labelView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
            labelView.setTextSize(11f);
            labelView.setPadding(0, dp(4), 0, 0);
            column.addView(labelView);
            addView(column);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
