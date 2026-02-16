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
        // ImageView ivLock;
        // RatingBar ratingBar;

        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tv_level_number);
        }

        void bind(LevelProgress level, OnLevelClickListener listener) {
            tvLevel.setText(String.valueOf(level.levelNumber));
            
            // Visual state based on level.isUnlocked
            if (level.isUnlocked) {
                itemView.setAlpha(1.0f);
                itemView.setBackgroundColor(0xFFE0E0E0); // Light Gray
            } else {
                itemView.setAlpha(0.5f);
                itemView.setBackgroundColor(0xFFBDBDBD); // Darker Gray
            }
            
            itemView.setOnClickListener(v -> {
                if (level.isUnlocked && listener != null) {
                    listener.onLevelClick(level.levelNumber);
                }
            });
        }
    }
    
    public interface OnLevelClickListener {
        void onLevelClick(int levelNumber);
    }
}
