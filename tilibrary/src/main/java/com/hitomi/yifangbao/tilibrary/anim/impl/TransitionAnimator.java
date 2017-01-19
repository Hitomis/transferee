package com.hitomi.yifangbao.tilibrary.anim.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.hitomi.yifangbao.tilibrary.anim.Location;
import com.hitomi.yifangbao.tilibrary.anim.ITransferAnimator;

import java.lang.reflect.Field;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransitionAnimator implements ITransferAnimator {

    private Context context;

    public TransitionAnimator(Context context) {
        this.context = context;
    }

    @Override
    public void showAnimator(final View sharedView, Location originViewLocation) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels - getStatusBarHeight();

        // 宽度变化
        ValueAnimator widthAnima = ValueAnimator.ofInt(originViewLocation.getWidth(), widthPixels);
        widthAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animaValue = Integer.parseInt(animation.getAnimatedValue().toString());
                ViewGroup.LayoutParams layoutParams = sharedView.getLayoutParams();
                layoutParams.width = animaValue;
                sharedView.setLayoutParams(layoutParams);
            }
        });

        // 高度变化
        ValueAnimator heightAnima = ValueAnimator.ofInt(originViewLocation.getHeight(), heightPixels);
        heightAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animaValue = Integer.parseInt(animation.getAnimatedValue().toString());
                ViewGroup.LayoutParams layoutParams = sharedView.getLayoutParams();
                layoutParams.height = animaValue;
                sharedView.setLayoutParams(layoutParams);
            }
        });

        // x 方向平移
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(sharedView, "x", originViewLocation.getX(), 0);
        // y 方向平移
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(sharedView, "y", originViewLocation.getY() - getStatusBarHeight(), 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(widthAnima)
                .with(heightAnima)
                .with(tranXAnima)
                .with(tranYAnima);
        animatorSet.setStartDelay(65);
        animatorSet.setDuration(300);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 显示高清图的加载进度 UI
            }
        });
        animatorSet.start();
    }

    @Override
    public void dismissAnimator(View sharedView) {

    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }
}
