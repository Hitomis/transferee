package com.hitomi.yifangbao.tilibrary.style.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;
import com.hitomi.yifangbao.tilibrary.style.Location;

import java.lang.reflect.Field;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransitionAnimator implements ITransferAnimator {

    private View originView;

    @Override
    public Animator showAnimator(View beforeView, View afterView) {
        originView = beforeView;
        AnimatorSet animatorSet = createTransferAnimator(afterView, false);
        animatorSet.setStartDelay(65);
        return animatorSet;
    }

    @Override
    public Animator dismissHitAnimator(View beforeView, View afterView) {
        originView = afterView;
        AnimatorSet animatorSet = createTransferAnimator(beforeView, true);
        animatorSet.setStartDelay(65);
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
                float scale = .8f * fraction + 1.f;
                float alpha = 1.f - fraction;

                beforeView.setScaleX(scale);
                beforeView.setScaleY(scale);
                beforeView.setAlpha(alpha);
            }
        });
        return missAnimator;
    }

    private AnimatorSet createTransferAnimator(final View sharedView, boolean reverse) {
        AnimatorConfig config = new AnimatorConfig(sharedView.getContext(), reverse).invoke();

        // 宽度变化
        ValueAnimator widthAnima = ValueAnimator.ofInt(config.getStartWidth(), config.getEndWidth());
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
        ValueAnimator heightAnima = ValueAnimator.ofInt(config.getStartHeight(), config.getEndHeight());
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
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(sharedView, "x", config.getStartTranX(), config.getEndTranX());
        // y 方向平移
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(sharedView, "y", config.getStartTranY(), config.getEndTranY());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(widthAnima)
                .with(heightAnima)
                .with(tranXAnima)
                .with(tranYAnima);
        animatorSet.setDuration(300);

        return animatorSet;
    }

    /**
     * 获取状态栏高度
     *
     * @return
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

    private class AnimatorConfig {
        private Context context;
        private boolean reverse;
        private Location originLocation;
        private int startWidth;
        private int endWidth;
        private int startHeight;
        private int endHeight;
        private float startTranX;
        private float endTranX;
        private float startTranY;
        private float endTranY;

        AnimatorConfig(Context context, boolean reverse) {
            this.context = context;
            this.reverse = reverse;
            this.originLocation = Location.converLocation(originView);
        }

        int getStartWidth() {
            return startWidth;
        }

        int getEndWidth() {
            return endWidth;
        }

        int getStartHeight() {
            return startHeight;
        }

        int getEndHeight() {
            return endHeight;
        }

        float getStartTranX() {
            return startTranX;
        }

        float getEndTranX() {
            return endTranX;
        }

        float getStartTranY() {
            return startTranY;
        }

        float getEndTranY() {
            return endTranY;
        }

        AnimatorConfig invoke() {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int widthPixels = displayMetrics.widthPixels;
            int heightPixels = displayMetrics.heightPixels - getStatusBarHeight(context);

            if (reverse) {
                startWidth = widthPixels;
                endWidth = originLocation.getWidth();

                startHeight = heightPixels;
                endHeight = originLocation.getHeight();

                startTranX = 0;
                endTranX = originLocation.getX();

                startTranY = 0;
                endTranY = originLocation.getY() - getStatusBarHeight(context);
            } else {
                startWidth = originLocation.getWidth();
                endWidth = widthPixels;

                startHeight = originLocation.getHeight();
                endHeight = heightPixels;

                startTranX = originLocation.getX();
                endTranX = 0;

                startTranY = originLocation.getY() - getStatusBarHeight(context);
                endTranY = 0;
            }
            return this;
        }
    }
}
