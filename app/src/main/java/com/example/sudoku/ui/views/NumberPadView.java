package com.example.sudoku.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.Button;
import android.graphics.Color;
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
        setColumnCount(3);
        setRowCount(3);
        
        for (int i = 1; i <= 9; i++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(com.example.sudoku.R.dimen.number_pad_button_size);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            
            Button button = new Button(context);
            button.setText(String.valueOf(i));
            button.setTextSize(24);
            button.setLayoutParams(params);
            button.setBackgroundColor(Color.LTGRAY); // Default
            button.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNumberClick(Integer.parseInt(((Button)v).getText().toString()));
                }
            });
            addView(button);
        }
    }
    
    public void updateTheme(int accentColor, int buttonBackgroundColor) {
        for (int i = 0; i < getChildCount(); i++) {
            android.view.View child = getChildAt(i);
            if (child instanceof Button) {
                ((Button) child).setTextColor(accentColor);
                ((Button) child).setBackgroundColor(buttonBackgroundColor);
            }
        }
    }
    
    public void setListener(OnNumberClickListener listener) {
        this.listener = listener;
    }
    
    public interface OnNumberClickListener {
        void onNumberClick(int number);
    }
}
