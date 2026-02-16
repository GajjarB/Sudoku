package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.R;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.ui.adapters.LevelGridAdapter;
import java.util.concurrent.Executors;

/**
 * Activity for selecting levels.
 */
public class LevelSelectActivity extends BaseActivity {
    
    private LevelRepository levelRepository;
    private LevelGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
        
        // Initialize Repository (In real app, use ViewModel)
        SudokuDatabase db = SudokuDatabase.getInstance(getApplicationContext());
        levelRepository = new LevelRepository(db.levelProgressDao());
        
        RecyclerView recyclerView = findViewById(R.id.recycler_levels);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        
        adapter = new LevelGridAdapter();
        adapter.setOnLevelClickListener(level -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("LEVEL_NUMBER", level);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        
        loadLevels();
    }
    
    private void loadLevels() {
        levelRepository.getUnlockedLevels().observe(this, levels -> {
            if (levels != null) {
                adapter.setLevels(levels);
            }
        });
    }
}
