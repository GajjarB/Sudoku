package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.sudoku.R;
import com.example.sudoku.data.preferences.PreferencesManager;
import com.example.sudoku.ui.fragments.GoalsFragment;
import com.example.sudoku.ui.fragments.HomeFragment;
import com.example.sudoku.ui.fragments.ProfileFragment;
import com.example.sudoku.ui.fragments.StatsFragment;
import com.example.sudoku.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStoredThemeMode();
        setContentView(R.layout.activity_main);

        String resumeId = getIntent().getStringExtra(Constants.EXTRA_RESUME_GAME);
        if (resumeId != null && savedInstanceState == null) {
            Intent resume = new Intent(MainActivity.this, GameActivity.class);
            resume.putExtra(Constants.EXTRA_GAME_ID, resumeId);
            startActivity(resume);
            finish();
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_goals) {
                showFragment(new GoalsFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_stats) {
                showFragment(new StatsFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                showFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        0,
                        R.anim.fade_in,
                        0
                )
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }

    private void applyStoredThemeMode() {
        String theme = new PreferencesManager(this).getTheme();
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("SYSTEM".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
