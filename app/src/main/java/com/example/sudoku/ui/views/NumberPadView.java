package com.example.sudoku.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.sudoku.R;
import com.example.sudoku.ui.utils.UiEffects;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom View representing the number pad (1-9).
 */
public class NumberPadView extends GridLayout {
    
    private OnNumberClickListener listener;
    private final Map<Integer, Button> valueButtons = new HashMap<>();

    public NumberPadView(Context context) {
        super(context);
        init(context);
    }

    public NumberPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setColumnCount(5);
        setRowCount(2);
        setUseDefaultMargins(false);
        setClipToPadding(false);

        for (int i = 1; i <= 10; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(R.dimen.number_pad_button_size);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            int margin = dpToPx(4);
            params.setMargins(margin, margin, margin, margin);

            Button button = new Button(context);
            final int value = i == 10 ? 0 : i;
            button.setText(i == 10
                    ? getResources().getString(R.string.control_clear)
                    : String.valueOf(i));
            button.setTextSize(i == 10 ? 15 : 20);
            button.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            button.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            button.setLayoutParams(params);
            button.setBackgroundResource(R.drawable.neo_number_button);
            button.setMinHeight(getResources().getDimensionPixelSize(R.dimen.number_pad_button_size));
            button.setAllCaps(false);
            button.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));
            button.setContentDescription(i == 10
                    ? getResources().getString(R.string.control_clear_content_description)
                    : getResources().getString(R.string.control_enter_number, i));
            button.setOnClickListener(v -> {
                selectButton((Button) v);
                if (listener != null) {
                    listener.onNumberClick(value);
                }
            });
            UiEffects.applyDuolingoPressEffect(button);
            addView(button);
            valueButtons.put(value, button);
        }
    }

    public void updateTheme(int accentColor, int buttonBackgroundColor) {
        for (int i = 0; i < getChildCount(); i++) {
            android.view.View child = getChildAt(i);
            if (child instanceof Button) {
                ((Button) child).setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                child.setBackgroundResource(R.drawable.neo_number_button);
            }
        }
    }

    private void selectButton(Button selected) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(getChildAt(i) == selected);
        }
    }

    public void setCompletedNumbers(boolean[] completed) {
        if (completed == null || completed.length < 10) return;
        for (int n = 1; n <= 9; n++) {
            Button button = valueButtons.get(n);
            if (button == null) continue;
            boolean isComplete = completed[n];
            button.setEnabled(!isComplete);
            button.setAlpha(isComplete ? 0.62f : 1f);
            button.setText(isComplete ? n + " ✓" : String.valueOf(n));
            button.setSelected(false);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
    
    public void setListener(OnNumberClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        listener = null;
        super.onDetachedFromWindow();
    }
    
    public interface OnNumberClickListener {
        void onNumberClick(int number);
    }
}
