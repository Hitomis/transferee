package com.hitomi.tilibrary.transfer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.hitomi.tilibrary.view.image.TransferImage;
import com.hitomi.tilibrary.view.video.ExoVideoView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * Created by Vans Z on 2019-11-05.
 */
class DragCloseGesture {
    private static int SOURCE_IMAGE = 1;
    private static int SOURCE_VIDEO = 2;

    private TransferLayout transferLayout;
    private VelocityTracker velocityTracker;
    private float preX;
    private float preY;
    private float scale; // 拖拽图片缩放值
    private int touchSlop;
    private int sourceType;
    private DragCloseListener dragListener;


    DragCloseGesture(TransferLayout transferLayout, DragCloseListener listener) {
        this.transferLayout = transferLayout;
        touchSlop = ViewConfiguration.get(transferLayout.getContext()).getScaledEdgeSlop();
        this.dragListener = listener;
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
                    sourceType = transferLayout.getTransConfig().isVideoSource(-1)
                            ? SOURCE_VIDEO : SOURCE_IMAGE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float diffY = ev.getRawY() - preY;
                    if (diffY > touchSlop) {
                        if (sourceType == SOURCE_IMAGE
                                && transferLayout.getCurrentImage().isScrollTop()) {
                            // 如果是图片，需要判断是否目前是顶部对齐（针对长图判断是不是滚动到顶部）
                            if (dragListener != null) dragListener.onDragStar();
                            return true;
                        } else if (sourceType == SOURCE_VIDEO) {
                            // 如果是视频，则直接返回 true
                            if (dragListener != null) dragListener.onDragStar();
                            return true;
                        }
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
                float scaleOffsetY = (1 - scale) * (1 - scale) * transferLayout.getHeight() * .5f;

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
                    transViewPager.setTranslationY(diffY - scaleOffsetY);
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
                    ImageView originImage = null;
                    if (!transferLayout.getTransConfig().getOriginImageList().isEmpty()) {
                        originImage = transferLayout.getTransConfig().getOriginImageList().get(pos);
                    }
                    if (originImage == null) { // 走扩散消失动画
                        transferLayout.diffusionTransfer(pos);
                    } else { // 走过渡动画
                        if (sourceType == SOURCE_IMAGE) {
                            startTransformImageAnimate(originImage);
                        } else {
                            startTransformVideoAnimate(originImage);
                        }
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

    /**
     * 当前是图片视图，且满足关闭条件执行关闭过渡动画
     */
    private void startTransformImageAnimate(ImageView originImage) {
        ViewPager transViewPagerUp = transferLayout.transViewPager;
        transViewPagerUp.setVisibility(View.INVISIBLE);
        TransferImage currTransImage = transferLayout.getCurrentImage();
        int[] location = new int[2];
        originImage.getLocationOnScreen(location);

        int paddingTop = transferLayout.getPaddingTop();
        int paddingBottom = transferLayout.getPaddingBottom();
        int x = location[0];
        int y = location[1] - paddingTop;
        int width = originImage.getWidth();
        int height = originImage.getHeight();

        TransferImage transImage = new TransferImage(transferLayout.getContext());
        transImage.setScaleType(FIT_CENTER);
        transImage.setOriginalInfo(x, y, width, height);
        transImage.setDuration(transferLayout.getTransConfig().getDuration());
        transImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        transImage.setOnTransferListener(transferLayout.transListener);
        transImage.setImageDrawable(currTransImage.getDrawable());

        float currTransWidth, currTransHeight;
        if (currTransImage.isEnableGesture()) { // 如果手势功能已经启用，表示图片是加载成功了
            float[] afterSize = currTransImage.getAfterTransferSize();
            currTransWidth = afterSize[0];
            currTransHeight = afterSize[1];
        } else {
            float[] beforeSize = currTransImage.getBeforeTransferSize(width, height);
            currTransWidth = beforeSize[0];
            currTransHeight = beforeSize[1];
        }
        float realWidth = currTransWidth * scale;
        float realHeight = currTransHeight * scale;
        float left = transViewPagerUp.getTranslationX()
                + (transferLayout.getWidth() - realWidth) * .5f;
        float top = transViewPagerUp.getTranslationY()
                + (transferLayout.getHeight() - paddingTop - paddingBottom - realHeight) * .5f;
        RectF rectF = new RectF(left, top, left + realWidth, top + realHeight);
        transImage.transformSpecOut(rectF, scale);
        transferLayout.addView(transImage, 1);
    }

    /**
     * 当前是视频视图，且满足关闭条件执行关闭过渡动画
     */
    private void startTransformVideoAnimate(ImageView originImage) {
        ViewPager transViewPagerUp = transferLayout.transViewPager;
        transViewPagerUp.setVisibility(View.INVISIBLE);
        ExoVideoView currVideo = transferLayout.getCurrentVideo();
        long duration = transferLayout.getTransConfig().getDuration();
        int[] location = new int[2];
        originImage.getLocationOnScreen(location);

        int paddingTop = transferLayout.getPaddingTop();
        int paddingBottom = transferLayout.getPaddingBottom();
        int x = location[0];
        int y = location[1] - paddingTop;
        int width = originImage.getWidth();
        int height = originImage.getHeight();

        TransferImage alphaOneImage = new TransferImage(transferLayout.getContext());
        alphaOneImage.setScaleType(FIT_CENTER);
        alphaOneImage.setOriginalInfo(x, y, width, height);
        alphaOneImage.setDuration(duration);
        alphaOneImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        alphaOneImage.setImageDrawable(originImage.getDrawable());
        alphaOneImage.setAlpha(0f);
        alphaOneImage.animate().alpha(1f).setDuration(duration);

        TransferImage alphaZeroImage = new TransferImage(transferLayout.getContext());
        alphaZeroImage.setScaleType(FIT_CENTER);
        alphaZeroImage.setOriginalInfo(x, y, width, height);
        alphaZeroImage.setDuration(duration);
        alphaZeroImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        alphaZeroImage.setOnTransferListener(transferLayout.transListener);
        alphaZeroImage.setImageBitmap(currVideo.getBitmap());
        alphaZeroImage.setAlpha(1f);
        alphaZeroImage.animate().alpha(0f).setDuration(duration);

        float realWidth = currVideo.getMeasuredWidth() * scale;
        float realHeight = currVideo.getMeasuredHeight() * scale;
        float left = transViewPagerUp.getTranslationX()
                + (transferLayout.getWidth() - realWidth) * .5f;
        float top = transViewPagerUp.getTranslationY()
                + (transferLayout.getHeight() - paddingTop - paddingBottom - realHeight) * .5f;
        RectF rectF = new RectF(left, top, left + realWidth, top + realHeight);
        alphaOneImage.transformSpecOut(rectF, scale);
        alphaZeroImage.transformSpecOut(rectF, scale);

        transferLayout.addView(alphaOneImage, 1);
        transferLayout.addView(alphaZeroImage, 2);
    }

    /**
     * 不满足关闭条件，执行回滚动画
     */
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

        bgColor.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dragListener != null) dragListener.onDragRollback();
            }
        });


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(bgColor, scaleX, scaleY, transX, transY);
        animatorSet.start();
    }


    public interface DragCloseListener {
        /**
         * 拖拽开始
         */
        void onDragStar();

        /**
         * 不满足拖拽返回条件，执行rollBack动画
         */
        void onDragRollback();
    }

}
