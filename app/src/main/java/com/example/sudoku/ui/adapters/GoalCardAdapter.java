package com.example.sudoku.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.R;
import com.example.sudoku.ui.utils.UiEffects;
import com.example.sudoku.ui.viewmodels.GoalsViewModel;

import java.util.ArrayList;
import java.util.List;

public class GoalCardAdapter extends RecyclerView.Adapter<GoalCardAdapter.GoalViewHolder> {

    public interface OnGoalActionListener {
        void onGoalAction(GoalsViewModel.GoalCardItem item);
    }

    private final List<GoalsViewModel.GoalCardItem> items = new ArrayList<>();
    private final OnGoalActionListener listener;

    public GoalCardAdapter(OnGoalActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GoalsViewModel.GoalCardItem> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalsViewModel.GoalCardItem item = items.get(position);
        holder.icon.setText(item.icon);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        holder.reward.setText(item.rewardXp + " XP");
        holder.progressBar.setProgress(item.target == 0 ? 0 : Math.round((item.progress * 100f) / item.target));
        holder.button.setText(item.actionLabel);
        UiEffects.applyDuolingoPressEffect(holder.button);
        holder.button.setOnClickListener(v -> listener.onGoalAction(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView title;
        final TextView subtitle;
        final TextView reward;
        final ProgressBar progressBar;
        final Button button;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tv_goal_icon);
            title = itemView.findViewById(R.id.tv_goal_title);
            subtitle = itemView.findViewById(R.id.tv_goal_subtitle);
            reward = itemView.findViewById(R.id.tv_goal_reward);
            progressBar = itemView.findViewById(R.id.progress_goal);
            button = itemView.findViewById(R.id.btn_goal_action);
        }
    }
}
