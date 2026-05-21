package com.example.sudoku.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.R;
import com.example.sudoku.ui.adapters.StaticListAdapter;
import java.util.List;

public abstract class StaticScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_screen);

        ((TextView) findViewById(R.id.tv_static_label)).setText(label());
        ((TextView) findViewById(R.id.tv_static_title)).setText(title());
        ((TextView) findViewById(R.id.tv_static_subtitle)).setText(subtitle());

        RecyclerView recycler = findViewById(R.id.recycler_static);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        StaticListAdapter adapter = new StaticListAdapter();
        recycler.setAdapter(adapter);
        adapter.setRows(rows());
    }

    protected abstract String label();
    protected abstract String title();
    protected abstract String subtitle();
    protected abstract List<StaticListAdapter.Row> rows();
}
