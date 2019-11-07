package com.hitomi.tilibrary.transfer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.view.image.TransferImage;

import java.lang.reflect.Field;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * Created by Vans Z on 2019-11-05.
 */
class DragCloseGesture {

    private TransferLayout transferLayout;
    private VelocityTracker velocityTracker;
    private float preX;
    private float preY;
    private float scale; // 拖拽图片缩放值
    private int touchSlop;


    DragCloseGesture(TransferLayout transferLayout) {
        this.transferLayout = transferLayout;
        touchSlop = ViewConfiguration.get(transferLayout.getContext()).getScaledEdgeSlop();
    }

    boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    preX = ev.getRawX();
                    preY = ev.getRawY();
                    if (null == velocityTracker) {
                        velocityTracker = VelocityTracker.obtain();
                    } else {
                        velocityTracker.clear();
                    }
                    velocityTracker.addMovement(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float diffY = ev.getRawY() - preY;
                    float diffX = Math.abs(ev.getRawX() - preX);
                    TransferImage currentImage = transferLayout.getCurrentImage();
                    if (diffX < touchSlop && diffY > touchSlop && currentImage.isScrollTop()) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    preY = 0;
                    break;
            }
        }
        return false;
    }

    void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = event.getRawX();
                preY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                float diffX = event.getRawX() - preX;
                float diffY = event.getRawY() - preY;
                float absDiffY = Math.abs(diffY);
                scale = 1 - absDiffY / transferLayout.getHeight() * .75f;

                if (absDiffY < 350) {
                    transferLayout.alpha = 255 - (absDiffY / 350) * 25;
                } else {
                    transferLayout.alpha = 230 - (absDiffY - 350) * 1.35f / transferLayout.getHeight() * 255;
                }

                transferLayout.alpha = transferLayout.alpha < 0 ? 0 : transferLayout.alpha;

                ViewPager transViewPager = transferLayout.transViewPager;
                if (transViewPager.getTranslationY() >= 0) {
                    transferLayout.setBackgroundColor(transferLayout.getBackgroundColorByAlpha(transferLayout.alpha));
                    transViewPager.setTranslationX(diffX);
                    transViewPager.setTranslationY(diffY);
                    transViewPager.setScaleX(scale);
                    transViewPager.setScaleY(scale);
                } else {
                    transferLayout.setBackgroundColor(transferLayout.getTransConfig().getBackgroundColor());
                    transViewPager.setTranslationX(diffX);
                    transViewPager.setTranslationY(diffY);
                }
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityY = velocityTracker.getYVelocity();
                if (velocityY > 100) {
                    int pos = transferLayout.getTransConfig().getNowThumbnailIndex();
                    ImageView originImage = transferLayout.getTransConfig().getOriginImageList().get(pos);
                    if (originImage == null) { // 走扩散消失动画
                        transferLayout.diffusionTransfer(pos);
                    } else { // 走过渡动画
                        startTransformAnima(pos, originImage);
                    }
                } else {
                    startFlingAndRollbackAnimation();
                }

                preX = 0;
                preY = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (null != velocityTracker) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
        }
    }

    private void startTransformAnima(int pos, ImageView originImage) {
        ViewPager transViewPagerUp = transferLayout.transViewPager;
        transViewPagerUp.setVisibility(View.INVISIBLE);
        int[] location = new int[2];
        originImage.getLocationInWindow(location);


        int x = location[0];
        int y = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ? location[1] : location[1] - getStatusBarHeight();
        int width = originImage.getWidth();
        int height = originImage.getHeight();


        TransferImage transImage = new TransferImage(transferLayout.getContext());
        transImage.setScaleType(FIT_CENTER);
        transImage.setOriginalInfo(x, y, width, height);
        transImage.setDuration(300);
        transImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        transImage.setOnTransferListener(transferLayout.transListener);
        transImage.setImageDrawable(transferLayout.transAdapter.getImageItem(pos).getDrawable());


        TransferImage currTransImage = transferLayout.getCurrentImage();
        float realWidth = currTransImage.getDeformedWidth() * scale;
        float realHeight = currTransImage.getDeformedHeight() * scale;
        float left = transViewPagerUp.getTranslationX() + (transferLayout.getWidth() - realWidth) * .5f;
        float top = transViewPagerUp.getTranslationY() + (transferLayout.getHeight() - realHeight) * .5f;
        RectF rectF = new RectF(left, top, realWidth, realHeight);
        transImage.transformSpecOut(rectF, scale);
        transferLayout.addView(transImage, 1);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return transferLayout.getContext().getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }

    private void startFlingAndRollbackAnimation() {
        ViewPager transViewPager = transferLayout.transViewPager;
        ValueAnimator bgColor = ObjectAnimator.ofFloat(null, "alpha", transferLayout.alpha, 255.f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(transViewPager, "scaleX", transViewPager.getScaleX(), 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(transViewPager, "scaleY", transViewPager.getScaleX(), 1.0f);
        ObjectAnimator transX = ObjectAnimator.ofFloat(transViewPager, "translationX", transViewPager.getTranslationX(), 0);
        ObjectAnimator transY = ObjectAnimator.ofFloat(transViewPager, "translationY", transViewPager.getTranslationY(), 0);

        bgColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = Float.parseFloat(animation.getAnimatedValue().toString());
                transferLayout.setBackgroundColor(transferLayout.getBackgroundColorByAlpha(value));
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(bgColor, scaleX, scaleY, transX, transY);
        animatorSet.start();
    }
}
