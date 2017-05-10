package com.hitomi.tilibrary.style.progress;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.filippudak.ProgressPieView.ProgressPieView;
import com.hitomi.tilibrary.style.IProgressIndicator;

import java.util.Locale;

/**
 * 图片加载时使用饼状并带进度百分比显示的进度组件
 * <a href= "https://github.com/FilipPudak/ProgressPieView">ProgressPieView</a>
 * <p>
 * email: 196425254@qq.com
 */
public class ProgressPieIndicator implements IProgressIndicator {

    private SparseArray<ProgressPieView> progressPieArray = new SparseArray<>();

    private int dip2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void attach(int position, FrameLayout parent) {
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

        parent.addView(progressPieView, parent.getChildCount());
        progressPieArray.put(position, progressPieView);
    }

    @Override
    public void hideView(int position) {
        ProgressPieView progressPieView = progressPieArray.get(position);
        if (progressPieView != null)
            progressPieView.setVisibility(View.GONE);
    }

    @Override
    public void onStart(int position) {
        ProgressPieView progressPieView = progressPieArray.get(position);
        progressPieView.setProgress(0);
        progressPieView.setText(String.format(Locale.getDefault(), "%d%%", 0));
    }

    @Override
    public void onProgress(int position, int progress) {
        if (progress < 0 || progress > 100) return;

        ProgressPieView progressPieView = progressPieArray.get(position);
        progressPieView.setProgress(progress);
        progressPieView.setText(String.format(Locale.getDefault(), "%d%%", progress));
    }

    @Override
    public void onFinish(int position) {
        ProgressPieView progressPieView = progressPieArray.get(position);
        if (progressPieView == null) return;

        ViewGroup vg = (ViewGroup) progressPieView.getParent();
        ;
        if (vg != null) {
            vg.removeView(progressPieView);
        }
    }
}
