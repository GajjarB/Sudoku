package com.example.sudoku.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * Animated Flame View for streaks.
 */
public class StreakFlameView extends View {

    private Paint flamePaint;
    private int streakDays = 0;

    public StreakFlameView(Context context) {
        super(context);
        init();
    }

    public StreakFlameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        flamePaint = new Paint();
        flamePaint.setColor(Color.parseColor("#FF5722")); // Deep Orange
        flamePaint.setStyle(Paint.Style.FILL);
        flamePaint.setAntiAlias(true);
    }
    
    public void setStreak(int days) {
        this.streakDays = days;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float cx = width / 2;
        float cy = height / 2;
        float radius = Math.min(width, height) / 3;
        
        if (streakDays > 0) {
            // Draw a simple flame circle for now
            // In real app, would be a complex path or drawable
            canvas.drawCircle(cx, cy, radius, flamePaint);
        }
    }
}
