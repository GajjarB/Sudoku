package com.example.sudoku.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;
import android.widget.Button;
import androidx.annotation.Nullable;

/**
 * Custom View representing the number pad (1-9).
 */
public class NumberPadView extends GridLayout {
    
    private OnNumberClickListener listener;

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
        
        for (int i = 1; i <= 10; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(com.example.sudoku.R.dimen.number_pad_button_size);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            int margin = dpToPx(4);
            params.setMargins(margin, margin, margin, margin);
            
            Button button = new Button(context);
            final int value = i == 10 ? 0 : i;
            button.setText(i == 10 ? "Del" : String.valueOf(i));
            button.setTextSize(22);
            button.setTextColor(getResources().getColor(com.example.sudoku.R.color.text_primary));
            button.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            button.setLayoutParams(params);
            button.setBackgroundResource(com.example.sudoku.R.drawable.neo_number_button);
            button.setMinHeight(getResources().getDimensionPixelSize(com.example.sudoku.R.dimen.number_pad_button_size));
            button.setAllCaps(false);
            button.setContentDescription(i == 10 ? "Delete selected cell" : "Enter " + i);
            button.setOnClickListener(v -> {
                selectButton((Button) v);
                if (listener != null) {
                    listener.onNumberClick(value);
                }
            });
            addView(button);
        }
    }
    
    public void updateTheme(int accentColor, int buttonBackgroundColor) {
        for (int i = 0; i < getChildCount(); i++) {
            android.view.View child = getChildAt(i);
            if (child instanceof Button) {
                ((Button) child).setTextColor(getResources().getColor(com.example.sudoku.R.color.text_primary));
                child.setBackgroundResource(com.example.sudoku.R.drawable.neo_number_button);
            }
        }
    }

    private void selectButton(Button selected) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(getChildAt(i) == selected);
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
