package com.hitomi.yifangbao.tilibrary.style.indicat;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.filippudak.ProgressPieView.ProgressPieView;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;

import java.util.Locale;

public class ProgressPieIndicator implements IProgressIndicator {
    private ProgressPieView progressPieView;

    private int dip2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public View getView(ViewGroup parent) {
        Context context = parent.getContext();

        int progressSize = dip2Px(context, 50);
        FrameLayout.LayoutParams progressLp = new FrameLayout.LayoutParams(
                progressSize, progressSize);
        progressLp.gravity = Gravity.CENTER;

        progressPieView = new ProgressPieView(context);
        progressPieView.setTextSize(13);
        progressPieView.setStrokeWidth(1);
        progressPieView.setTextColor(Color.WHITE);
        progressPieView.setProgressFillType(ProgressPieView.FILL_TYPE_RADIAL);
        progressPieView.setBackgroundColor(Color.TRANSPARENT);
        progressPieView.setProgressColor(Color.parseColor("#BBFFFFFF"));
        progressPieView.setStrokeColor(Color.WHITE);
        progressPieView.setLayoutParams(progressLp);

        parent.addView(progressPieView);
        return progressPieView;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onProgress(int progress) {
        if (progress < 0 || progress > 100) {
            return;
        }
        progressPieView.setProgress(progress);
        progressPieView.setText(String.format(Locale.getDefault(), "%d%%", progress));
    }

    @Override
    public void onFinish() {
        progressPieView.setVisibility(View.GONE);
    }
}
