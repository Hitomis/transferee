package com.hitomi.tilibrary.view.image;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * TransferImage 主要功能：<br/>
 * <p>
 * 可以完成从缩略图平滑伸展到一张完整的图片<br/>
 * 也可以从整图平滑收缩到一张缩略图
 * <ul>
 * <li>支持动画：从缩略图平滑伸展到一张完整的图片</li>
 * <li>支持动画：从整图平滑收缩到一张缩略图</li>
 * <li>支持按指定尺寸参数裁剪后，在裁剪的区域显示图片</li>
 * <li>支持动画分离：只有图片平移动画或者只有图片缩放动画</li>
 * </ul>
 * email: 196425254@qq.com
 */
public class TransferImage extends PhotoView {

    public static final int STATE_TRANS_NORMAL = 0; // 普通状态
    public static final int STATE_TRANS_IN = 1; // 从缩略图到大图状态
    public static final int STATE_TRANS_OUT = 2; // 从大图到缩略图状态
    public static final int STATE_TRANS_CLIP = 3; // 裁剪状态

    public static final int CATE_ANIMA_TOGETHER = 100; // 动画类型：位移和缩放同时进行
    public static final int CATE_ANIMA_APART = 200; // 动画类型：位移和缩放分开进行

    public static final int STAGE_TRANSLATE = 201; // 平移
    public static final int STAGE_SCALE = 202; // 缩放

    private int state = STATE_TRANS_NORMAL; // 当前动画状态
    private int cate = CATE_ANIMA_TOGETHER; // 当前动画类型
    private int stage = STAGE_TRANSLATE; // 针对 CATE_ANIMA_APART 类型对话而言：当前动画的阶段

    private int originalWidth;
    private int originalHeight;
    private int originalLocationX;
    private int originalLocationY;
    private long duration = 300; // 默认动画时长
    private boolean transformStart = false; // 开始动画的标记

    private Paint paint;
    private Matrix transMatrix;

    private Transfrom transform;
    private OnTransferListener transformListener;

    public TransferImage(Context context) {
        this(context, null);
    }

    public TransferImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        transMatrix = new Matrix();
        paint = new Paint();
    }

    /**
     * 设置 TransferImage 初始位置信息
     *
     * @param locationX x坐标位置
     * @param locationY y坐标位置
     * @param width     宽度
     * @param height    高度
     */
    public void setOriginalInfo(int locationX, int locationY, int width, int height) {
        originalLocationX = locationX;
        originalLocationY = locationY;
        originalWidth = width;
        originalHeight = height;
    }

    /**
     * 设置 TransferImage 初始位置信息
     *
     * @param targetDrawable 初始显示的图片 Drawable
     * @param originWidth    TransferImage 初始宽度
     * @param originHeight   TransferImage 初始高度
     * @param width          容器宽度
     * @param height         容器高度
     */
    public void setOriginalInfo(Drawable targetDrawable, int originWidth, int originHeight, int width, int height) {
        Rect rect = getClipOriginalInfo(targetDrawable, originWidth, originHeight, width, height);
        originalLocationX = rect.left;
        originalLocationY = rect.top;
        originalWidth = rect.right;
        originalHeight = rect.bottom;
    }

    private Rect getClipOriginalInfo(Drawable targetDrawable, int originWidth, int originHeight, int width, int height) {
        Rect rect = new Rect();

        float xSScale = originWidth / ((float) targetDrawable.getIntrinsicWidth());
        float ySScale = originHeight / ((float) targetDrawable.getIntrinsicHeight());
        float endScale = xSScale > ySScale ? xSScale : ySScale;

        float drawableEndWidth = targetDrawable.getIntrinsicWidth() * endScale;
        float drawableEndHeight = targetDrawable.getIntrinsicHeight() * endScale;

        rect.left = (int) ((width - drawableEndWidth) / 2);
        rect.top = (int) ((height - drawableEndHeight) / 2);
        rect.right = (int) drawableEndWidth;
        rect.bottom = (int) drawableEndHeight;

        return rect;
    }

    /**
     * 按 {@link #setOriginalInfo(int, int, int, int)} 方法指定的的参数裁剪显示的图片
     */
    public void transClip() {
        state = STATE_TRANS_CLIP;
        transformStart = true;
    }

    /**
     * 用于开始进入的方法。 调用此方前，需已经调用过setOriginalInfo
     */
    public void transformIn() {
        cate = CATE_ANIMA_TOGETHER;
        state = STATE_TRANS_IN;
        transformStart = true;

        paint.setAlpha(0);
        invalidate();
    }

    /**
     * 用于开始进入的方法(平移和放大动画分离)。 调用此方前，需已经调用过setOriginalInfo
     *
     * @param animaStage 动画阶段 :{@link #STAGE_TRANSLATE} 平移，{@link #STAGE_SCALE}
     */
    public void transformIn(int animaStage) {
        cate = CATE_ANIMA_APART;
        state = STATE_TRANS_IN;
        stage = animaStage;
        transformStart = true;

        if (stage == STAGE_TRANSLATE)
            paint.setAlpha(0);
        else
            paint.setAlpha(255);
        invalidate();
    }

    /**
     * 用于开始退出的方法。 调用此方前，需已经调用过setOriginalInfo
     */
    public void transformOut() {
        cate = CATE_ANIMA_TOGETHER;
        state = STATE_TRANS_OUT;
        transformStart = true;

        paint.setAlpha(255);
        invalidate();
    }

    /**
     * 用于开始退出的方法(平移和放大动画分离)。 调用此方前，需已经调用过setOriginalInfo
     *
     * @param animaStage 动画阶段 :{@link #STAGE_TRANSLATE} 平移，{@link #STAGE_SCALE}
     */
    public void transformOut(int animaStage) {
        cate = CATE_ANIMA_APART;
        state = STATE_TRANS_OUT;
        stage = animaStage;
        transformStart = true;

        paint.setAlpha(255);
        invalidate();
    }

    /**
     * 获取伸缩动画执行的时间
     *
     * @return unit ：毫秒
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 设置伸缩动画执行的时间
     *
     * @param duration unit ：毫秒
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 获取当前的状态
     *
     * @return {@link #STATE_TRANS_NORMAL}, {@link #STATE_TRANS_IN}, {@link #STATE_TRANS_OUT}, {@link #STATE_TRANS_CLIP}
     */
    public int getState() {
        return state;
    }

    /**
     * 设置当前动画的状态
     *
     * @param state {@link #STATE_TRANS_NORMAL}, {@link #STATE_TRANS_IN}, {@link #STATE_TRANS_OUT}, {@link #STATE_TRANS_CLIP}
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * 为 TransferImage 视图设置背景颜色
     *
     * @param color the new color (including alpha) to set in the paint.
     */
    public void setBackgroundColor(int color) {
        paint.setColor(color);
    }

    /**
     * 初始化进入的变量信息
     */
    private void initTransform() {
        Drawable transDrawable = getDrawable();
        if (transDrawable == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        transform = new Transfrom();

        /** 下面为缩放的计算 */
        /* 计算初始的缩放值，初始值因为是CENTR_CROP效果，所以要保证图片的宽和高至少1个能匹配原始的宽和高，另1个大于 */
        float xSScale = originalWidth / ((float) transDrawable.getIntrinsicWidth());
        float ySScale = originalHeight / ((float) transDrawable.getIntrinsicHeight());
        float startScale = xSScale > ySScale ? xSScale : ySScale;
        transform.startScale = startScale;
        /* 计算结束时候的缩放值，结束值因为要达到FIT_CENTER效果，所以要保证图片的宽和高至少1个能匹配原始的宽和高，另1个小于 */
        float xEScale = getWidth() / ((float) transDrawable.getIntrinsicWidth());
        float yEScale = getHeight() / ((float) transDrawable.getIntrinsicHeight());
        float endScale = xEScale < yEScale ? xEScale : yEScale;
        if (cate == CATE_ANIMA_APART && stage == STAGE_TRANSLATE) { // 平移阶段的动画，不缩放
            transform.endScale = startScale;
        } else {
            transform.endScale = endScale;
        }

        /**
         * 计算Canvas Clip的范围，也就是图片的显示的范围，因为图片是慢慢变大，并且是等比例的，所以这个效果还需要裁减图片显示的区域
         * ，而显示区域的变化范围是在原始CENTER_CROP效果的范围区域
         * ，到最终的FIT_CENTER的范围之间的，区域我用LocationSizeF更好计算
         * ，他就包括左上顶点坐标，和宽高，最后转为Canvas裁减的Rect.
         */
        /* 开始区域 */
        transform.startRect = new LocationSizeF();
        transform.startRect.left = originalLocationX;
        transform.startRect.top = originalLocationY;
        transform.startRect.width = originalWidth;
        transform.startRect.height = originalHeight;
        /* 结束区域 */
        transform.endRect = new LocationSizeF();
        float bitmapEndWidth = transDrawable.getIntrinsicWidth() * transform.endScale;// 图片最终的宽度
        float bitmapEndHeight = transDrawable.getIntrinsicHeight() * transform.endScale;// 图片最终的高度
        transform.endRect.left = (getWidth() - bitmapEndWidth) / 2;
        transform.endRect.top = (getHeight() - bitmapEndHeight) / 2;
        transform.endRect.width = bitmapEndWidth;
        transform.endRect.height = bitmapEndHeight;

        transform.rect = new LocationSizeF();
    }

    private void calcBmpMatrix() {
        Drawable transDrawable = getDrawable();
        if (transDrawable == null || transform == null) return;

		/* 下面实现了CENTER_CROP的功能 */
        transMatrix.setScale(transform.scale, transform.scale);
        transMatrix.postTranslate(-(transform.scale * transDrawable.getIntrinsicWidth() / 2 - transform.rect.width / 2),
                -(transform.scale * transDrawable.getIntrinsicHeight() / 2 - transform.rect.height / 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) return;

        if (state != STATE_TRANS_NORMAL) {
            if (transformStart) {
                initTransform();
            }
            if (transform == null) {
                super.onDraw(canvas);
                return;
            }

            if (transformStart) {
                switch (state) {
                    case STATE_TRANS_IN:
                        transform.initStartIn();
                        break;
                    case STATE_TRANS_OUT:
                        transform.initStartOut();
                        break;
                    case STATE_TRANS_CLIP:
                        paint.setAlpha(255);
                        transform.initStartClip();
                        break;
                }
            }

            canvas.drawPaint(paint);

            int saveCount = canvas.getSaveCount();
            canvas.save();
            // 先得到图片在此刻的图像Matrix矩阵
            calcBmpMatrix();
            canvas.translate(transform.rect.left, transform.rect.top);
            canvas.clipRect(0, 0, transform.rect.width, transform.rect.height);
            canvas.concat(transMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);
            if (transformStart && state != STATE_TRANS_CLIP) {
                transformStart = false;

                switch (cate) {
                    case CATE_ANIMA_TOGETHER:
                        startTogetherTrans();
                        break;
                    case CATE_ANIMA_APART:
                        startApartTrans();
                        break;
                }
            }
        } else {
            paint.setAlpha(255);
            canvas.drawPaint(paint);
            super.onDraw(canvas);
        }
    }

    private void startApartTrans() {
        if (transform == null) return;

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        if (stage == STAGE_TRANSLATE) { // 平移动画
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", transform.startRect.left, transform.endRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", transform.startRect.top, transform.endRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", transform.startRect.width, transform.endRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", transform.startRect.height, transform.endRect.height);
            valueAnimator.setValues(leftHolder, topHolder, widthHolder, heightHolder);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public synchronized void onAnimationUpdate(ValueAnimator animation) {
                    paint.setAlpha((int) (255 * animation.getAnimatedFraction()));
                    transform.rect.left = (Float) animation.getAnimatedValue("left");
                    transform.rect.top = (Float) animation.getAnimatedValue("top");
                    transform.rect.width = (Float) animation.getAnimatedValue("width");
                    transform.rect.height = (Float) animation.getAnimatedValue("height");
                    invalidate();
                }
            });
        } else { // 缩放动画
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", transform.startRect.left, transform.endRect.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", transform.startRect.top, transform.endRect.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", transform.startRect.width, transform.endRect.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", transform.startRect.height, transform.endRect.height);
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", transform.startScale, transform.endScale);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public synchronized void onAnimationUpdate(ValueAnimator animation) {
                    transform.rect.left = (Float) animation.getAnimatedValue("left");
                    transform.rect.top = (Float) animation.getAnimatedValue("top");
                    transform.rect.width = (Float) animation.getAnimatedValue("width");
                    transform.rect.height = (Float) animation.getAnimatedValue("height");
                    transform.scale = (Float) animation.getAnimatedValue("scale");
                    invalidate();
                }
            });
        }

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (stage == STAGE_TRANSLATE) {
                    originalLocationX = (int) transform.endRect.left;
                    originalLocationY = (int) transform.endRect.top;
                    originalWidth = (int) transform.endRect.width;
                    originalHeight = (int) transform.endRect.height;
                }

                if (state == STATE_TRANS_IN && stage == STAGE_SCALE)
                    TransferImage.this.state = STATE_TRANS_NORMAL;

                if (transformListener != null)
                    transformListener.onTransferComplete(state, cate, stage);

            }
        });

        if (state == STATE_TRANS_IN)
            valueAnimator.start();
        else
            valueAnimator.reverse();
    }

    private void startTogetherTrans() {
        if (transform == null) return;

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", transform.startScale, transform.endScale);
        PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", transform.startRect.left, transform.endRect.left);
        PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", transform.startRect.top, transform.endRect.top);
        PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", transform.startRect.width, transform.endRect.width);
        PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", transform.startRect.height, transform.endRect.height);
        valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                paint.setAlpha((int) (255 * animation.getAnimatedFraction()));
                transform.scale = (Float) animation.getAnimatedValue("scale");
                transform.rect.left = (Float) animation.getAnimatedValue("left");
                transform.rect.top = (Float) animation.getAnimatedValue("top");
                transform.rect.width = (Float) animation.getAnimatedValue("width");
                transform.rect.height = (Float) animation.getAnimatedValue("height");
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (transformListener != null)
                    transformListener.onTransferComplete(state, cate, stage);

                /*
                 * 如果是进入的话，当然是希望最后停留在center_crop的区域。但是如果是out的话，就不应该是center_crop的位置了
				 * ， 而应该是最后变化的位置，因为当out的时候结束时，不回复视图是Normal，要不然会有一个突然闪动回去的bug
				 */
                if (state == STATE_TRANS_IN)
                    TransferImage.this.state = STATE_TRANS_NORMAL;

            }
        });

        if (state == STATE_TRANS_IN)
            valueAnimator.start();
        else
            valueAnimator.reverse();

    }

    public void setOnTransferListener(OnTransferListener listener) {
        transformListener = listener;
    }

    public interface OnTransferListener {
        /**
         * @param state {@link #STATE_TRANS_IN} {@link #STATE_TRANS_OUT}
         * @param cate  {@link #CATE_ANIMA_TOGETHER} {@link #CATE_ANIMA_APART}
         * @param stage {@link #STAGE_TRANSLATE} {@link #STAGE_SCALE}
         */
        void onTransferComplete(int state, int cate, int stage);
    }

    private class Transfrom {
        float startScale;// 图片开始的缩放值
        float endScale;// 图片结束的缩放值
        float scale;// 属性ValueAnimator计算出来的值
        LocationSizeF startRect;// 开始的区域
        LocationSizeF endRect;// 结束的区域
        LocationSizeF rect;// 属性ValueAnimator计算出来的值

        void initStartIn() {
            scale = startScale;
            try {
                rect = (LocationSizeF) startRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        void initStartOut() {
            scale = endScale;
            try {
                rect = (LocationSizeF) endRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        void initStartClip() {
            scale = startScale;
            try {
                rect = (LocationSizeF) endRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

    }

    private class LocationSizeF implements Cloneable {
        float left;
        float top;
        float width;
        float height;

        @Override
        public String toString() {
            return "[left:" + left + " top:" + top + " width:" + width + " height:" + height + "]";
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

}
