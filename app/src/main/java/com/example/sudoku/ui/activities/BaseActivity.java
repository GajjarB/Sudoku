package com.example.sudoku.ui.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sudoku.utils.ThemeStorage;
import com.example.sudoku.utils.ThemeUtils;

/**
 * Base Activity to handle global theme application.
 */
public abstract class BaseActivity extends AppCompatActivity {
    
    protected ThemeUtils.Theme currentTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Load theme before calling super.onCreate or setContentView
        currentTheme = ThemeStorage.loadTheme(this);
        
        super.onCreate(savedInstanceState);
        
        // Apply window background
        getWindow().getDecorView().setBackgroundColor(currentTheme.backgroundColor);
        
        // Window transitions
        if (savedInstanceState == null) {
            overridePendingTransition(com.example.sudoku.R.anim.slide_in_right, com.example.sudoku.R.anim.slide_out_left);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(com.example.sudoku.R.anim.slide_in_left, com.example.sudoku.R.anim.slide_out_right);
    }
    
    protected void applyThemeToView(android.view.View view) {
        if (view == null) return;
        view.setBackgroundColor(currentTheme.backgroundColor);
    }
}
