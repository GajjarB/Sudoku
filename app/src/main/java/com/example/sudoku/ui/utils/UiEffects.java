package com.example.sudoku.ui.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public final class UiEffects {

    private UiEffects() {
    }

    public static void applyDuolingoPressEffect(View... views) {
        if (views == null) return;
        for (View view : views) {
            if (view == null) continue;
            view.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    v.animate()
                            .cancel();
                    v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .translationY(dpToPx(v, 3))
                            .setDuration(70)
                            .start();
                } else if (action == MotionEvent.ACTION_UP) {
                    v.animate()
                            .cancel();
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(170)
                            .setInterpolator(new OvershootInterpolator(1.1f))
                            .start();
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    v.animate()
                            .cancel();
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(120)
                            .start();
                }
                return false;
            });
        }
    }

    private static float dpToPx(View view, float dp) {
        return dp * view.getResources().getDisplayMetrics().density;
    }
}
