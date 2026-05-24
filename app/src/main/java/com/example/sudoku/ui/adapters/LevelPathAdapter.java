package com.example.sudoku.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.R;
import com.example.sudoku.data.local.entities.LevelProgress;

import java.util.ArrayList;
import java.util.List;

public class LevelPathAdapter extends RecyclerView.Adapter<LevelPathAdapter.LevelViewHolder> {

    public interface OnLevelClickListener {
        void onLevelClicked(LevelProgress level);
    }

    private final List<LevelProgress> items = new ArrayList<>();
    private final OnLevelClickListener listener;
    private int activeLevel = 1;

    public LevelPathAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<LevelProgress> levels) {
        items.clear();
        if (levels != null) {
            items.addAll(levels);
        }
        notifyDataSetChanged();
    }

    public void setActiveLevel(int activeLevel) {
        this.activeLevel = Math.max(1, activeLevel);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_node, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        LevelProgress level = items.get(position);
        boolean leftAligned = position % 2 == 0;
        holder.leftCard.setVisibility(leftAligned ? View.VISIBLE : View.INVISIBLE);
        holder.rightCard.setVisibility(leftAligned ? View.INVISIBLE : View.VISIBLE);
        holder.connector.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);

        LinearLayout card = leftAligned ? holder.leftCard : holder.rightCard;
        TextView icon = leftAligned ? holder.leftIcon : holder.rightIcon;
        TextView title = leftAligned ? holder.leftTitle : holder.rightTitle;
        TextView subtitle = leftAligned ? holder.leftSubtitle : holder.rightSubtitle;

        title.setText("Level " + level.levelNumber);
        subtitle.setText(buildSubtitle(level));
        icon.setText(resolveIcon(level));
        icon.setBackgroundResource(resolveIconBackground(level));
        card.setAlpha(level.isUnlocked ? 1f : 0.72f);
        card.setOnClickListener(level.isUnlocked ? v -> listener.onLevelClicked(level) : null);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildSubtitle(LevelProgress level) {
        if (level.isCompleted) {
            return level.levelNumber % 10 == 0 ? "Boss cleared" : "Completed";
        }
        if (!level.isUnlocked) {
            return level.levelNumber % 10 == 0 ? "Boss locked" : "Locked";
        }
        if (level.levelNumber == activeLevel) {
            return "Current puzzle";
        }
        if (level.levelNumber % 5 == 0) {
            return "Challenge node";
        }
        return difficultyLabel(level.levelNumber);
    }

    private String difficultyLabel(int levelNumber) {
        if (levelNumber <= 5) return "Easy basics";
        if (levelNumber <= 20) return "Medium logic";
        if (levelNumber <= 50) return "Hard focus";
        return "Expert climb";
    }

    private String resolveIcon(LevelProgress level) {
        if (!level.isUnlocked) return "🔒";
        if (level.isCompleted) return "✓";
        if (level.levelNumber % 10 == 0) return "★";
        if (level.levelNumber % 5 == 0) return "♛";
        if (level.levelNumber == activeLevel) return "▶";
        return "●";
    }

    private int resolveIconBackground(LevelProgress level) {
        if (!level.isUnlocked) return R.drawable.bg_level_locked;
        if (level.isCompleted) return R.drawable.bg_level_completed;
        if (level.levelNumber % 10 == 0) return R.drawable.bg_level_boss;
        if (level.levelNumber % 5 == 0) return R.drawable.bg_level_challenge;
        return R.drawable.bg_level_current;
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout leftCard;
        final LinearLayout rightCard;
        final TextView leftIcon;
        final TextView rightIcon;
        final TextView leftTitle;
        final TextView rightTitle;
        final TextView leftSubtitle;
        final TextView rightSubtitle;
        final View connector;

        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            leftCard = itemView.findViewById(R.id.card_left);
            rightCard = itemView.findViewById(R.id.card_right);
            leftIcon = itemView.findViewById(R.id.icon_left);
            rightIcon = itemView.findViewById(R.id.icon_right);
            leftTitle = itemView.findViewById(R.id.title_left);
            rightTitle = itemView.findViewById(R.id.title_right);
            leftSubtitle = itemView.findViewById(R.id.subtitle_left);
            rightSubtitle = itemView.findViewById(R.id.subtitle_right);
            connector = itemView.findViewById(R.id.node_connector);
        }
    }
}
