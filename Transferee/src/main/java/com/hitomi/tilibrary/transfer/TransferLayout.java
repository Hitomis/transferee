package com.hitomi.tilibrary.transfer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import androidx.viewpager.widget.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Transferee 中 Dialog 显示的内容
 * <p>
 * 所有过渡动画的展示，图片的加载都是在这个 FrameLayout 中实现
 * <p>
 * Created by Hitomis on 2017/4/23 0023.
 * <p>
 * email: 196425254@qq.com
 */
class TransferLayout extends FrameLayout {
    private Context context;

    private TransferImage transImage;
    private TransferConfig transConfig;
    private DragCloseGesture dragCloseGesture;

    private OnLayoutResetListener layoutResetListener;
    private Set<Integer> loadedIndexSet;

    TransferAdapter transAdapter;
    ViewPager transViewPager;
    float alpha; // [0.f , 255.f]

    /**
     * 构造方法
     *
     * @param context 上下文环境
     */
    TransferLayout(Context context) {
        super(context);

        this.context = context;
        this.loadedIndexSet = new HashSet<>();
    }

    /**
     * 拖拽开始和未满足拖拽返回执行的rollBack回调
     */
    private DragCloseGesture.DragCloseListener dragCloseListener = new DragCloseGesture.DragCloseListener() {
        @Override public void onDragStar() {
            if(!transConfig.isEnableDragHide())return;
            View view = transConfig.getCustomView();
            if(view != null){
                view.setVisibility(GONE);
            }
            IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
            if(indexIndicator != null && transConfig.getSourceImageList().size() >= 2){
                indexIndicator.onHide();
            }
        }

        @Override public void onDragRollback() {
            if(!transConfig.isEnableDragHide())return;
            View view = transConfig.getCustomView();
            if(view != null){
                view.setVisibility(VISIBLE);
            }
            IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
            if(indexIndicator != null && transConfig.getSourceImageList().size() >= 2){
                indexIndicator.onShow(transViewPager);
            }
        }
    };

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            transConfig.setNowThumbnailIndex(position);

            if (transConfig.isJustLoadHitImage()) {
                loadSourceImageOffset(position, 0);
            } else {
                for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                    loadSourceImageOffset(position, i);
                }
            }
        }
    };
    /**
     * TransferAdapter 中对应页面创建完成监听器
     */
    private TransferAdapter.OnInstantiateItemListener instantListener = new TransferAdapter.OnInstantiateItemListener() {
        @Override
        public void onComplete() {
            transViewPager.addOnPageChangeListener(transChangeListener);

            int position = transConfig.getNowThumbnailIndex();
            if (transConfig.isJustLoadHitImage()) {
                loadSourceImageOffset(position, 0);
            } else {
                loadSourceImageOffset(position, 1);
            }

        }
    };
    /**
     * TransferImage 伸/缩动画执行完成监听器
     */
    TransferImage.OnTransferListener transListener = new TransferImage.OnTransferListener() {
        @Override
        public void onTransferStart(int state, int cate, int stage) {
        }

        @Override
        public void onTransferUpdate(int state, float fraction) {
            alpha = (state == TransferImage.STATE_TRANS_SPEC_OUT ? alpha : 255) * fraction;
            setBackgroundColor(getBackgroundColorByAlpha(alpha));
        }

        @Override
        public void onTransferComplete(int state, int cate, int stage) {
            if (cate == TransferImage.CATE_ANIMA_TOGETHER) {
                switch (state) {
                    case TransferImage.STATE_TRANS_IN: // 伸展动画执行完毕
                        addIndexIndicator();
                        addCustomView();
                        transViewPager.setVisibility(View.VISIBLE);
                        removeFromParent(transImage);
                        break;
                    case TransferImage.STATE_TRANS_OUT: // 缩小动画执行完毕
                    case TransferImage.STATE_TRANS_SPEC_OUT:
                        resetTransfer();
                        break;
                }
            } else { // 如果动画是分离的
                switch (state) {
                    case TransferImage.STATE_TRANS_IN:
                        if (stage == TransferImage.STAGE_TRANSLATE) {
                            // 第一阶段位移动画执行完毕
                            addIndexIndicator();
                            addCustomView();
                            transViewPager.setVisibility(View.VISIBLE);
                            removeFromParent(transImage);
                        }
                        break;
                    case TransferImage.STATE_TRANS_OUT:
                        if (stage == TransferImage.STAGE_TRANSLATE) {
                            // 位移动画执行完毕
                            resetTransfer();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            if (dragCloseGesture != null && dragCloseGesture.onInterceptTouchEvent(ev)) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (dragCloseGesture != null)
            dragCloseGesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    /**
     * 获取带透明度的颜色值
     *
     * @param alpha [1, 255]
     * @return color int value
     */
    int getBackgroundColorByAlpha(float alpha) {
        int bgColor = transConfig.getBackgroundColor();
        return Color.argb(Math.round(alpha), Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
    }

    /**
     * 加载 [position - offset] 到 [position + offset] 范围内有效索引位置的图片
     *
     * @param position 当前显示图片的索引
     * @param offset   postion 左右便宜量
     */
    private void loadSourceImageOffset(int position, int offset) {
        int left = position - offset;
        int right = position + offset;

        if (!loadedIndexSet.contains(position)) {
            loadSourceImage(position);
            loadedIndexSet.add(position);
        }
        if (left >= 0 && !loadedIndexSet.contains(left)) {
            loadSourceImage(left);
            loadedIndexSet.add(left);
        }
        if (right < transConfig.getSourceImageList().size() && !loadedIndexSet.contains(right)) {
            loadSourceImage(right);
            loadedIndexSet.add(right);
        }
    }

    /**
     * 加载索引位置为 position 处的图片
     *
     * @param position 当前有效的索引
     */
    private void loadSourceImage(int position) {
        getTransferState(position).transferLoad(position);
    }

    /**
     * 重置 TransferLayout 布局中的内容
     */
    private void resetTransfer() {
        loadedIndexSet.clear();
        removeIndexIndicator();
        removeAllViews();
        layoutResetListener.onReset();
    }

    /**
     * 创建 ViewPager 并添加到 TransferLayout 中
     */
    private void createTransferViewPager() {
        transAdapter = new TransferAdapter(this,
                transConfig.getSourceImageList().size(),
                transConfig.getNowThumbnailIndex());
        transAdapter.setOnInstantListener(instantListener);

        transViewPager = new ViewPager(context);
        // 先隐藏，待 ViewPager 下标为 config.getCurrOriginIndex() 的页面创建完毕再显示
        transViewPager.setVisibility(View.INVISIBLE);
        transViewPager.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() + 1);
        transViewPager.setAdapter(transAdapter);
        transViewPager.setCurrentItem(transConfig.getNowThumbnailIndex());

        addView(transViewPager, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    /**
     * 将 view 从 view 的父布局中移除
     *
     * @param view 待移除的 view
     */
    private void removeFromParent(View view) {
        ViewGroup vg = (ViewGroup) view.getParent();
        if (vg != null)
            vg.removeView(view);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // unregister PageChangeListener
        transViewPager.removeOnPageChangeListener(transChangeListener);
    }

    TransferConfig getTransConfig() {
        return transConfig;
    }

    TransferImage getCurrentImage() {
        return transAdapter.getImageItem(transViewPager.getCurrentItem());
    }


    /**
     * 初始化 TransferLayout 中的各个组件，并执行图片从缩略图到 Transferee 进入动画
     */
    void show() {
        createTransferViewPager();

        int nowThumbnailIndex = transConfig.getNowThumbnailIndex();
        TransferState transferState = getTransferState(nowThumbnailIndex);
        transImage = transferState.createTransferIn(nowThumbnailIndex);
    }

    /**
     * 依据当前有效索引 position 创建并返回一个 {@link TransferState}
     *
     * @param position 前有效索引
     * @return {@link TransferState}
     */
    TransferState getTransferState(int position) {
        TransferState transferState;

        if (!transConfig.isThumbnailEmpty()) { // 客户端指定了缩略图路径集合
            transferState = new RemoteThumbState(this);
        } else {
            String url = transConfig.getSourceImageList().get(position);

            // 即使是网络图片，但是之前已经加载到本地，那么也是本地图片
            if (transConfig.getImageLoader().getCache(url) != null) {
                transferState = new LocalThumbState(this);
            } else {
                transferState = new EmptyThumbState(this);
            }
        }

        return transferState;
    }

    /**
     * 为加载完的成图片ImageView 绑定点 Transferee 操作事件
     *
     * @param imageView 加载完成的 ImageView
     * @param pos       关闭 Transferee 时图片所在的索引
     */
    void bindOnOperationListener(final ImageView imageView, final String imageUri, final int pos) {
        // bind click dismiss listener
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(pos);
            }
        });

        // bind long click listener
        if (transConfig.getLongClickListener() != null)
            imageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    transConfig.getLongClickListener().onLongClick(imageView, imageUri, pos);
                    return false;
                }
            });
    }

    /**
     * 开启 Transferee 关闭动画，并隐藏 transferLayout 中的各个组件
     *
     * @param pos 关闭 Transferee 时图片所在的索引
     */
    void dismiss(int pos) {
        if (transImage != null && transImage.getState()
                == TransferImage.STATE_TRANS_OUT) // 防止双击
            return;

        transImage = getTransferState(pos).transferOut(pos);

        if (transImage == null)
            diffusionTransfer(pos);
        else
            transViewPager.setVisibility(View.INVISIBLE);

        hideIndexIndicator();

    }

    /**
     * 扩散消失动画
     *
     * @param pos 动画作用于 pos 索引位置的图片
     */
    void diffusionTransfer(int pos) {
        transImage = transAdapter.getImageItem(pos);
        transImage.setState(TransferImage.STATE_TRANS_OUT);
        transImage.disable();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(transConfig.getDuration());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", alpha, 0.f);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1.f, 1.2f);
        valueAnimator.setValues(alphaHolder, scaleXHolder);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue("alpha");
                float scale = (Float) animation.getAnimatedValue("scaleX");

                setBackgroundColor(getBackgroundColorByAlpha(alpha));
                transImage.setAlpha(alpha / 255.f);
                transImage.setScaleX(scale);
                transImage.setScaleY(scale);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetTransfer();
            }
        });

        valueAnimator.start();
    }

    /**
     * 配置参数
     *
     * @param config 参数对象
     */
    void apply(TransferConfig config) {
        transConfig = config;
        if (transConfig.isEnableDragClose())
            this.dragCloseGesture = new DragCloseGesture(this, dragCloseListener);
    }

    /**
     * 绑定 TransferLayout 内容重置时回调监听器
     *
     * @param listener 重置回调监听器
     */
    void setOnLayoutResetListener(OnLayoutResetListener listener) {
        layoutResetListener = listener;
    }

    /**
     * 在 TransferImage 面板中添加下标指示器 UI 组件
     */
    private void addIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.attach(this);
            indexIndicator.onShow(transViewPager);
        }
    }

    /**
     * 隐藏下标指示器 UI 组件
     */
    private void hideIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.onHide();
        }
    }

    private void addCustomView() {
        View customView = transConfig.getCustomView();
        if (customView != null) {
            addView(customView);
        }
    }

    /**
     * 从 TransferImage 面板中移除下标指示器 UI 组件
     */
    private void removeIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.onRemove();
        }
    }

    /**
     * TransferLayout 中内容重置时监听器
     */
    interface OnLayoutResetListener {
        /**
         * 调用于：当关闭动画执行完毕，TransferLayout 中所有内容已经重置（清空）时
         */
        void onReset();
    }

}
