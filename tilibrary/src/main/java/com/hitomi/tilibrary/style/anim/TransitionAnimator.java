package com.hitomi.tilibrary.style.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.hitomi.tilibrary.style.ITransferAnimator;
import com.hitomi.tilibrary.style.Location;

import java.lang.reflect.Field;

/**
 * Created by hitomi on 2017/1/19.
 *
 */
public class TransitionAnimator implements ITransferAnimator {

    @Override
    public Animator showAnimator(View beforeView, View afterView) {
        Context context = afterView.getContext();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels - getStatusBarHeight(context);

        Location originLocation = Location.convertLocation(beforeView);
        int startTranX = originLocation.getX();
        int endTranX = (widthPixels - originLocation.getWidth()) / 2;

        int startTranY = originLocation.getY() - getStatusBarHeight(context);
        int endTranY = (heightPixels - originLocation.getHeight()) / 2;

        float endScaleVal = widthPixels * 1.f / originLocation.getRealWidth();

        // x 方向放大
        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(afterView, "scaleX", afterView.getScaleX(), endScaleVal);
        // y 方向放大
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(afterView, "scaleY", afterView.getScaleY(), endScaleVal);
        // x 方向平移
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(afterView, "x", startTranX, endTranX);
        // y 方向平移
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(afterView, "y", startTranY, endTranY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(tranXAnima)
                .with(tranYAnima).with(scaleXAnima).with(scaleYAnima);
        animatorSet.setDuration(300);
        animatorSet.setStartDelay(65);
        return animatorSet;
    }

    @Override
    public Animator dismissHitAnimator(View beforeView, View afterView) {
        Location location = Location.convertLocation(afterView);

        float endScale = location.getRealWidth() * 1.f / beforeView.getWidth();
        float endScaleY = (afterView.getHeight() * 1.f / beforeView.getHeight());
        float endTranX = (beforeView.getWidth() - (beforeView.getWidth() * endScale)) * .5f - location.getRealX();
        float endTranY = (beforeView.getHeight() - (beforeView.getHeight() * endScaleY)) * .5f
                - (location.getY() - getStatusBarHeight(beforeView.getContext()));

        // x 方向缩小
        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(beforeView, "scaleX", beforeView.getScaleX(), endScale);
        // y 方向缩小
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(beforeView, "scaleY", beforeView.getScaleY(), endScale);
        // x 方向平移
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(beforeView, "x", beforeView.getTranslationX(), -endTranX);
        // y 方向平移
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(beforeView, "y", beforeView.getTranslationY(), -endTranY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.play(scaleXAnima)
                .with(scaleYAnima)
                .with(tranXAnima)
                .with(tranYAnima);
        return animatorSet;
    }

    @Override
    public Animator dismissMissAnimator(final View beforeView) {
        ValueAnimator missAnimator = ValueAnimator.ofFloat(0, 1f);
        missAnimator.setInterpolator(new AccelerateInterpolator());
        missAnimator.setDuration(350);
        missAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float scale = .5f * fraction + 1.f;
                float alpha = 1.f - fraction;

                beforeView.setScaleX(scale);
                beforeView.setScaleY(scale);
                beforeView.setAlpha(alpha);

            }
        });
        return missAnimator;
    }

    @Override
    public Animator dismissBackgroundAnimator(final View parent, final int backgroundColor) {
        ValueAnimator backAnimator = ValueAnimator.ofFloat(1.f - Color.alpha(backgroundColor) / 255.f, 1f);
        backAnimator.setInterpolator(new AccelerateInterpolator());
        backAnimator.setDuration(350);
        backAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) (255 * (1.f - Float.parseFloat(animation.getAnimatedValue().toString())));
                int color = ColorUtils.setAlphaComponent(backgroundColor, alpha);
                parent.setBackgroundColor(color);

            }
        });
        return backAnimator;
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    private int getStatusBarHeight(Context context) {
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
