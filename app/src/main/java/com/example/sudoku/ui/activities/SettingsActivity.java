package com.example.sudoku.ui.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.sudoku.R;
import com.example.sudoku.utils.ThemeStorage;
import com.example.sudoku.utils.ThemeUtils;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Since we don't have a layout xml for this yet, we'll create a simple list view programmatically
        // or layout file. Let's create a layout file next.
        setContentView(R.layout.activity_settings);
        
        ListView listView = findViewById(R.id.settings_list);
        
        String[] settings = {"Theme", "About"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, settings);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                showThemeDialog();
            } else if (position == 1) {
                Toast.makeText(this, "Sudoku Nexus v1.0\nDustland Style", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Update Title Color if needed or handle in XML
    }
    
    private void showThemeDialog() {
        String[] themes = new String[ThemeUtils.Theme.values().length];
        for (int i = 0; i < themes.length; i++) {
            themes[i] = ThemeUtils.Theme.values()[i].name;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setItems(themes, (dialog, which) -> {
                    ThemeUtils.Theme selectedTheme = ThemeUtils.Theme.values()[which];
                    ThemeStorage.saveTheme(this, selectedTheme);
                    
                    // Recreate activity to apply new theme
                    recreate();
                    
                    // Also need to notify other activities? 
                    // Usually returning to them is enough if they handle onResume or recreate.
                    // For now, recreate this one.
                })
                .show();
    }
}
