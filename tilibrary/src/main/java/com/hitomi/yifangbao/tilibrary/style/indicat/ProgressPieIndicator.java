package com.hitomi.yifangbao.tilibrary.style.indicat;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.filippudak.ProgressPieView.ProgressPieView;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProgressPieIndicator implements IProgressIndicator {

    private Map<Integer, ProgressPieView> progressPieViewMap = new HashMap<>();

    private int dip2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public View getView(int position, ViewGroup parent) {
        Context context = parent.getContext();

        int progressSize = dip2Px(context, 50);
        FrameLayout.LayoutParams progressLp = new FrameLayout.LayoutParams(
                progressSize, progressSize);
        progressLp.gravity = Gravity.CENTER;

        ProgressPieView progressPieView = new ProgressPieView(context);
        progressPieView.setTextSize(13);
        progressPieView.setStrokeWidth(1);
        progressPieView.setTextColor(Color.WHITE);
        progressPieView.setProgressFillType(ProgressPieView.FILL_TYPE_RADIAL);
        progressPieView.setBackgroundColor(Color.TRANSPARENT);
        progressPieView.setProgressColor(Color.parseColor("#BBFFFFFF"));
        progressPieView.setStrokeColor(Color.WHITE);
        progressPieView.setLayoutParams(progressLp);

        parent.addView(progressPieView);
        progressPieViewMap.put(position, progressPieView);

        return progressPieView;
    }

    @Override
    public void onStart(int position) {
    }

    @Override
    public void onProgress(int position, int progress) {
        if (progress < 0 || progress > 100) {
            return;
        }
        ProgressPieView progressPieView = progressPieViewMap.get(position);
        progressPieView.setProgress(progress);
        progressPieView.setText(String.format(Locale.getDefault(), "%d%%", progress));
    }

    @Override
    public void onFinish(int position) {
        progressPieViewMap.get(position).setVisibility(View.GONE);
    }
}
