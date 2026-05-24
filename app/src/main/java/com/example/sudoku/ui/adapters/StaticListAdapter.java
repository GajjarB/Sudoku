package com.example.sudoku.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.R;
import java.util.ArrayList;
import java.util.List;

public class StaticListAdapter extends RecyclerView.Adapter<StaticListAdapter.RowViewHolder> {

    private final List<Row> rows = new ArrayList<>();

    public void setRows(List<Row> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_static_row, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView icon;
        private final TextView title;
        private final TextView subtitle;
        private final TextView value;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tv_row_icon);
            title = itemView.findViewById(R.id.tv_row_title);
            subtitle = itemView.findViewById(R.id.tv_row_subtitle);
            value = itemView.findViewById(R.id.tv_row_value);
        }

        void bind(Row row) {
            icon.setText(row.icon);
            title.setText(row.title);
            subtitle.setText(row.subtitle);
            value.setText(row.value);
            value.setVisibility(row.value == null || row.value.trim().isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    public static class Row {
        public final String icon;
        public final String title;
        public final String subtitle;
        public final String value;

        public Row(String icon, String title, String subtitle, String value) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.value = value;
        }
    }
}
