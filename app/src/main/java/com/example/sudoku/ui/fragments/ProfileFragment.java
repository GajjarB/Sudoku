package com.example.sudoku.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sudoku.R;
import com.example.sudoku.ui.activities.PremiumActivity;
import com.example.sudoku.ui.activities.SettingsActivity;
import com.example.sudoku.ui.activities.ThemesActivity;
import com.example.sudoku.ui.utils.UiEffects;
import com.example.sudoku.ui.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        TextView avatar = view.findViewById(R.id.tv_profile_avatar);
        TextView name = view.findViewById(R.id.tv_profile_name);
        TextView rank = view.findViewById(R.id.tv_profile_rank);
        TextView xp = view.findViewById(R.id.tv_profile_xp);
        ProgressBar xpProgress = view.findViewById(R.id.progress_profile_xp);
        LinearLayout achievements = view.findViewById(R.id.container_achievements);
        LinearLayout customization = view.findViewById(R.id.container_customization);
        LinearLayout settings = view.findViewById(R.id.container_settings);
        Button upgrade = view.findViewById(R.id.btn_upgrade);

        upgrade.setOnClickListener(v -> startActivity(new Intent(requireContext(), PremiumActivity.class)));
        UiEffects.applyDuolingoPressEffect(upgrade);

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            avatar.setText(state.avatarLetter);
            name.setText(state.name);
            rank.setText(state.rank);
            xp.setText(state.xpText);
            xpProgress.setProgress(state.xpProgress);
            populateAchievements(achievements, state);
            populateRows(customization, state.customization);
            populateRows(settings, state.settings);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void populateAchievements(LinearLayout container, ProfileViewModel.ProfileUiState state) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ProfileViewModel.AchievementItem item : state.achievements) {
            View row = inflater.inflate(R.layout.item_achievement, container, false);
            ((TextView) row.findViewById(R.id.tv_achievement_icon)).setText(item.icon);
            ((TextView) row.findViewById(R.id.tv_achievement_title)).setText(item.title);
            ((TextView) row.findViewById(R.id.tv_achievement_subtitle)).setText(item.subtitle + (item.unlocked ? "" : " • Locked"));
            row.setAlpha(item.unlocked ? 1f : 0.72f);
            container.addView(row);
        }
    }

    private void populateRows(LinearLayout container, java.util.List<ProfileViewModel.RowItem> items) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ProfileViewModel.RowItem item : items) {
            View row = inflater.inflate(R.layout.item_setting_row, container, false);
            ((TextView) row.findViewById(R.id.tv_row_title)).setText(item.title);
            ((TextView) row.findViewById(R.id.tv_row_subtitle)).setText(item.subtitle);
            ((TextView) row.findViewById(R.id.tv_row_value)).setText(item.action == null ? "—" : ">");
            row.setOnClickListener(item.action == null ? null : v -> openAction(item.action));
            container.addView(row);
        }
    }

    private void openAction(String action) {
        if ("themes".equals(action)) {
            startActivity(new Intent(requireContext(), ThemesActivity.class));
        } else if ("settings".equals(action)) {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        }
    }
}
