package com.example.sudoku.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sudoku.R;
import com.example.sudoku.ui.viewmodels.GameViewModel;
import com.example.sudoku.ui.views.NumberPadView;
import com.example.sudoku.ui.views.SudokuGridView;
import com.example.sudoku.ui.views.TimerView;

/**
 * The main Gameplay Activity.
 */
public class GameActivity extends BaseActivity {

    private GameViewModel viewModel;
    private SudokuGridView gridView;
    private TimerView timerView;
    private TextView mistakeText;
    
    // currentTheme is inherited from BaseActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        
        gridView = findViewById(R.id.sudoku_grid);
        NumberPadView numberPad = findViewById(R.id.number_pad);
        timerView = findViewById(R.id.timer_view);
        mistakeText = findViewById(R.id.tv_mistakes);
        
        // Control Buttons
        android.widget.Button btnUndo = findViewById(R.id.btn_undo);
        android.widget.Button btnErase = findViewById(R.id.btn_erase);
        android.widget.Button btnNotes = findViewById(R.id.btn_notes);
        android.widget.Button btnHint = findViewById(R.id.btn_hint);

        btnUndo.setOnClickListener(v -> viewModel.undo());
        btnErase.setOnClickListener(v -> viewModel.erase());
        btnNotes.setOnClickListener(v -> viewModel.toggleNotesMode());
        btnHint.setOnClickListener(v -> viewModel.hint());
        
        // Theme Update helper for controls?
        // We'll update them in applyTheme too.
        
        viewModel.getNotesMode().observe(this, isNotes -> {
             if (Boolean.TRUE.equals(isNotes)) {
                 btnNotes.setBackgroundColor(currentTheme.accentColor);
                 btnNotes.setTextColor(android.graphics.Color.WHITE);
             } else {
                 btnNotes.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                 btnNotes.setTextColor(com.example.sudoku.utils.ThemeUtils.Theme.BLUE.equals(currentTheme) ? 
                         android.graphics.Color.BLACK : currentTheme.primaryTextColor); // Robust text color
             }
        });
        
        // Setup Grid Interaction
        gridView.setListener((row, col) -> viewModel.onCellClicked(row, col));
        
        // Setup Number Pad
        numberPad.setListener(number -> viewModel.onNumberInput(number));
        
        // Observe ViewModel
        viewModel.getGrid().observe(this, grid -> gridView.setGrid(grid));
        
        viewModel.getMistakes().observe(this, mistakes -> {
            mistakeText.setText("Mistakes: " + mistakes + "/3");
        });
        
        viewModel.getGameState().observe(this, state -> {
            if (state == com.example.sudoku.domain.models.GameState.PLAYING) {
                timerView.start();
            } else {
                timerView.stop();
                if (state == com.example.sudoku.domain.models.GameState.WON) {
                    android.widget.Toast.makeText(this, "PUZZLE SOLVED!", android.widget.Toast.LENGTH_LONG).show();
                    // Navigate back or show dialog
                } else if (state == com.example.sudoku.domain.models.GameState.GAME_OVER) {
                    android.widget.Toast.makeText(this, "GAME OVER", android.widget.Toast.LENGTH_LONG).show();
                }
            }
        });
        
        // Apply theme to specific views
        applyTheme_Local(currentTheme);
        
        // Start game
        if (getIntent().hasExtra("GAME_ID")) {
            String gameId = getIntent().getStringExtra("GAME_ID");
            viewModel.loadSavedGame(gameId);
        } else {
            int level = getIntent().getIntExtra("LEVEL_NUMBER", 1);
            viewModel.startNewGame(level);
        }
    }

    private void applyTheme_Local(com.example.sudoku.utils.ThemeUtils.Theme theme) {
        // BaseActivity handles Window background.
        // We need to handle specific text and view colors here.
        
        // Apply to Grid
        gridView.updateTheme(theme.accentColor, theme.backgroundColor, theme.primaryTextColor, theme.gridLineColor);
        
        // Apply to Controls
        int textColor = theme.primaryTextColor;
        ((android.widget.Button)findViewById(R.id.btn_undo)).setTextColor(textColor);
        ((android.widget.Button)findViewById(R.id.btn_erase)).setTextColor(textColor);
        ((android.widget.Button)findViewById(R.id.btn_hint)).setTextColor(textColor);
        // Notes button handled by observer, but set base
        ((android.widget.Button)findViewById(R.id.btn_notes)).setTextColor(textColor);
        
        // Apply to NumberPad
        ((NumberPadView)findViewById(R.id.number_pad)).updateTheme(theme.accentColor, theme.secondaryColor);
        
        // Apply to Text
        mistakeText.setTextColor(theme.primaryTextColor);
        timerView.setTextColor(theme.primaryTextColor);
    }
}
