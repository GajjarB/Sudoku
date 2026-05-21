package com.example.sudoku.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.R;
import com.example.sudoku.data.local.entities.LevelProgress;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Level Selection Grid.
 */
public class LevelGridAdapter extends RecyclerView.Adapter<LevelGridAdapter.LevelViewHolder> {

    private List<LevelProgress> levels = new ArrayList<>();
    private OnLevelClickListener listener;

    public void setLevels(List<LevelProgress> levels) {
        this.levels = levels;
        notifyDataSetChanged();
    }
    
    public void setOnLevelClickListener(OnLevelClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We'll dynamic create view or inflate if layout exists
        // For now, let's inflate a simple item layout (we'll assume item_level.xml exists)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        LevelProgress level = levels.get(position);
        holder.bind(level, listener);
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel;
        TextView tvMeta;
        TextView tvStatus;

        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tv_level_number);
            tvMeta = itemView.findViewById(R.id.tv_level_meta);
            tvStatus = itemView.findViewById(R.id.tv_level_status);
        }

        void bind(LevelProgress level, OnLevelClickListener listener) {
            tvLevel.setText(String.format("#%03d", level.levelNumber));
            tvMeta.setText(difficultyLabel(level.levelNumber));
            tvStatus.setText(level.isCompleted ? "Completed" : level.isUnlocked ? "Not started" : "Locked");
            
            // Visual state based on level.isUnlocked
            if (level.isUnlocked) {
                itemView.setAlpha(1.0f);
            } else {
                itemView.setAlpha(0.5f);
            }
            
            itemView.setOnClickListener(v -> {
                if (level.isUnlocked && listener != null) {
                    listener.onLevelClick(level.levelNumber);
                }
            });
        }

        private String difficultyLabel(int level) {
            if (level <= 24) return "Easy";
            if (level <= 49) return "Medium";
            if (level <= 74) return "Hard";
            return "Expert";
        }
    }
    
    public interface OnLevelClickListener {
        void onLevelClick(int levelNumber);
    }
}
