package com.hitomi.tilibrary.transfer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;
import com.vansz.exoplayer.ExoVideoView;

import java.util.HashSet;
import java.util.List;
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
        @Override
        public void onDragStar() {
            if (transConfig.isEnableDragHide()) {
                hideIndexIndicator();
                hideCustomView();
            }
            if (transConfig.isEnableDragPause() && transConfig.isVideoSource(-1)) {
                transAdapter.getVideoItem(transConfig.getNowThumbnailIndex()).pause();
            }
        }

        @Override
        public void onDragRollback() {
            if (transConfig.isEnableDragHide()) {
                showIndexIndicator(false);
                showCustomView(false);
            }
            if (transConfig.isEnableDragPause() && transConfig.isVideoSource(-1)) {
                transAdapter.getVideoItem(transConfig.getNowThumbnailIndex()).resume();
            }
        }
    };

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            transConfig.setNowThumbnailIndex(position);

            if (transConfig.isJustLoadHitPage()) {
                loadSourceViewOffset(position, 0);
            } else {
                for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                    loadSourceViewOffset(position, i);
                }
            }
            controlVideoState(position);
            controlScrollingWithPageChange(position);
            // controlScrollingWithPageChange 会异步更新 originImageList，
            // 所以这里也需要使用线程队列去保证在之后执行 controlThumbHide
            post(new Runnable() {
                @Override
                public void run() {
                    controlThumbHide(position);
                }
            });

        }

        /**
         * 页面切换的时候，如果开启了 enableScrollingWithPageChange,
         * 需要实时检查当满足条件时滚动列表， 并更新 OriginImageList
         */
        private void controlScrollingWithPageChange(int position) {
            if (!transConfig.isEnableScrollingWithPageChange()) return;
            RecyclerView recyclerView = transConfig.getRecyclerView();
            AbsListView absListView = transConfig.getListView();
            if (recyclerView == null && absListView == null) return;
            View scrollView = recyclerView == null ? absListView : recyclerView;
            int headerSize = transConfig.getHeaderSize();
            int footerSize = transConfig.getFooterSize();

            int firstVisiblePos = -1, lastVisiblePos = -1;
            if (recyclerView != null) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayManager = ((GridLayoutManager) layoutManager);
                    firstVisiblePos = gridLayManager.findFirstVisibleItemPosition() - headerSize;
                    lastVisiblePos = gridLayManager.findLastVisibleItemPosition()
                            - headerSize - footerSize;
                } else if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayManager = ((LinearLayoutManager) layoutManager);
                    firstVisiblePos = linearLayManager.findFirstVisibleItemPosition() - headerSize;
                    lastVisiblePos = linearLayManager.findLastVisibleItemPosition()
                            - headerSize - footerSize;
                }

            } else {
                firstVisiblePos = absListView.getFirstVisiblePosition() - headerSize;
                lastVisiblePos = absListView.getLastVisiblePosition() - headerSize - footerSize;
            }

            // item 在列表可见范围之内， 不需要处理
            if (position >= firstVisiblePos && position <= lastVisiblePos) return;
            if (position < firstVisiblePos) { // 跳转位置在第一个可见项之前
                if (recyclerView != null) {
                    recyclerView.scrollToPosition(position);
                } else {
                    absListView.setSelection(position);
                }

            } else { // 跳转位置在最后可见项之后
                if (recyclerView != null) {
                    recyclerView.scrollToPosition(position + 1);
                    recyclerView.scrollToPosition(position);
                } else {
                    absListView.setSelection(position + 1);
                    absListView.setSelection(position);
                }
            }
            // 执行到这里说明一定发生过滚动
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    OriginalViewHelper.getInstance().fillOriginImages(transConfig);
                }
            });
        }

        /**
         * 页面切换的时候，如果开启了 enableHideThumb, 那么需要实时隐藏 originImage
         */
        private void controlThumbHide(int position) {
            if (!transConfig.isEnableHideThumb()) return;
            List<ImageView> originImageList = transConfig.getOriginImageList();
            for (int i = 0; i < originImageList.size(); i++) {
                ImageView currOriginImage = originImageList.get(i);
                if (currOriginImage != null) {
                    currOriginImage.setVisibility(i == position ? View.GONE : View.VISIBLE);
                }
            }
        }

        /**
         * 页面切换的时候，如果当前 position 是视频就播放，其他位置如果有视频，全部重置
         */
        private void controlVideoState(int position) {
            SparseArray<FrameLayout> cacheItems = transAdapter.getCacheItems();
            for (int i = 0; i < cacheItems.size(); i++) {
                int key = cacheItems.keyAt(i);
                View view = cacheItems.get(key).getChildAt(0);
                if (view instanceof ExoVideoView) {
                    ExoVideoView videoView = ((ExoVideoView) view);
                    if (key == position) {
                        videoView.resume();
                    } else {
                        videoView.reset();
                    }
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
            if (transConfig.isJustLoadHitPage()) {
                loadSourceViewOffset(position, 0);
            } else {
                loadSourceViewOffset(position, 1);
            }
            // 初始化的时候打开的就是视频, 那么打开后即刻开始播放 position 位置处的视频
            ExoVideoView videoView = transAdapter.getVideoItem(position);
            if (videoView != null) {
                videoView.resume();
            }
        }
    };
    /**
     * TransferImage 伸/缩动画执行完成监听器
     */
    TransferImage.OnTransferListener transListener = new TransferImage.OnTransferListener() {
        @Override
        public void onTransferStart(int state, int cate, int stage) {
            if (state == TransferImage.STATE_TRANS_IN) {
                if (transConfig.isEnableHideThumb()) {
                    ImageView originImage = transConfig.getOriginImageList().get(transConfig.getNowThumbnailIndex());
                    if (originImage != null) {
                        originImage.setVisibility(View.GONE);
                    }
                }
            }
        }

        @Override
        public void onTransferUpdate(int state, float fraction) {
            alpha = (state == TransferImage.STATE_TRANS_SPEC_OUT ? alpha : 255) * fraction;
            setBackgroundColor(getBackgroundColorByAlpha(alpha));

            // 因为在 onTransferComplete 中执行 originImage 的显示
            // 会出现闪现的问题。故需要提前一点点时间去先显示 originImage
            if (transConfig.isEnableHideThumb() && fraction <= 0.05 &&
                    (state == TransferImage.STATE_TRANS_OUT || state == TransferImage.STATE_TRANS_SPEC_OUT)) {
                ImageView originImage = transConfig.getOriginImageList().get(transConfig.getNowThumbnailIndex());
                if (originImage != null) {
                    originImage.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onTransferComplete(int state, int cate, int stage) {
            if (cate == TransferImage.CATE_ANIMA_TOGETHER) {
                switch (state) {
                    case TransferImage.STATE_TRANS_IN: // 伸展动画执行完毕
                        resumeTransfer();
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
                            resumeTransfer();
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
    private void loadSourceViewOffset(int position, int offset) {
        int left = position - offset;
        int right = position + offset;

        if (!loadedIndexSet.contains(position)) {
            loadSourceView(position);
            loadedIndexSet.add(position);
        }
        if (left >= 0 && !loadedIndexSet.contains(left)) {
            loadSourceView(left);
            loadedIndexSet.add(left);
        }
        if (right < transConfig.getSourceImageList().size() && !loadedIndexSet.contains(right)) {
            loadSourceView(right);
            loadedIndexSet.add(right);
        }
    }

    /**
     * 加载索引位置为 position 处的视图
     *
     * @param position 当前有效的索引
     */
    private void loadSourceView(int position) {
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
     * transferee STATE_TRANS_IN 动画执行完毕后，开始显示内容
     */
    private void resumeTransfer() {
        showIndexIndicator(true);
        showCustomView(true);
        transViewPager.setVisibility(View.VISIBLE);
        // 因为视频尺寸需要自适应屏幕宽度，渲染时机会延迟，所以将由
        // VideoThumbState 内部自己处理 transImage 的移除工作
        if (transImage != null && !transConfig.isVideoSource(-1))
            removeFromParent(transImage);
    }

    /**
     * 创建 ViewPager 并添加到 TransferLayout 中
     */
    private void createTransferViewPager(TransferState transferState) {
        transAdapter = new TransferAdapter(this,
                transConfig.getSourceImageList().size(),
                transConfig.getNowThumbnailIndex());
        transAdapter.setOnInstantListener(instantListener);

        transViewPager = new ViewPager(context);
        if (transferState instanceof NoneThumbState) {
            // 如果是 NoneThumbState 状态下，需要执行 alpha 显示动画
            transViewPager.setVisibility(View.VISIBLE);
            setBackgroundColor(getBackgroundColorByAlpha(255));
        } else {
            // 先隐藏，待 ViewPager 下标为 config.getCurrOriginIndex() 的页面创建完毕再显示
            transViewPager.setVisibility(View.INVISIBLE);
        }
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
    public void removeFromParent(View view) {
        if (view == null) return;
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

    ExoVideoView getCurrentVideo() {
        return transAdapter.getVideoItem(transViewPager.getCurrentItem());
    }


    /**
     * 初始化 TransferLayout 中的各个组件，并执行图片从缩略图到 Transferee 进入动画
     */
    void show() {
        int nowThumbnailIndex = transConfig.getNowThumbnailIndex();
        TransferState transferState = getTransferState(nowThumbnailIndex);
        createTransferViewPager(transferState);
        transImage = transferState.transferIn(nowThumbnailIndex);
    }

    /**
     * 依据当前有效索引 position 创建并返回一个 {@link TransferState}
     *
     * @param position 前有效索引
     * @return {@link TransferState}
     */
    TransferState getTransferState(int position) {
        TransferState transferState;

        if (transConfig.getOriginImageList().isEmpty()) { // 用户没有绑定任何 View
            transferState = new NoneThumbState(this);
        } else if (!transConfig.isThumbnailEmpty()) { // 客户端指定了缩略图路径集合
            transferState = new RemoteThumbState(this);
        } else {
            String url = transConfig.getSourceImageList().get(position);

            if (transConfig.isVideoSource(position)) {
                transferState = new VideoThumbState(this);
            } else if (transConfig.getImageLoader().getCache(url) != null) {
                // 即使是网络图片，但是之前已经加载到本地，那么也是本地图片
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
     * alpha 动画显示 transfer
     */
    void displayTransfer() {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 0.f, 1.f);
        ValueAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.2f, 1.0f);
        ValueAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.2f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnim, scaleX, scaleY);
        animatorSet.setDuration(transConfig.getDuration());
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resumeTransfer();
            }
        });
        animatorSet.start();
    }

    /**
     * 扩散消失动画
     *
     * @param pos 动画作用于 pos 索引位置的图片
     */
    void diffusionTransfer(int pos) {
        TransferImage diffTransImage = transAdapter.getImageItem(pos);
        if (diffTransImage != null) {
            diffTransImage.disable();
        }
        ExoVideoView diffVideoView = transAdapter.getVideoItem(pos);
        if (diffVideoView != null) {
            diffVideoView.pause();
        }

        float scale = transViewPager.getScaleX();
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", alpha, 0.f);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scale", scale, scale + .2f);
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(transConfig.getDuration());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setValues(alphaHolder, scaleXHolder);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue("alpha");
                float scale = (Float) animation.getAnimatedValue("scale");

                setBackgroundColor(getBackgroundColorByAlpha(alpha));
                transViewPager.setAlpha(alpha / 255.f);
                transViewPager.setScaleX(scale);
                transViewPager.setScaleY(scale);
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
     * 在 TransferImage 面板中添加显示下标指示器 UI 组件并显示
     *
     * @param init true 表示第一次初始化，需要先添加到页面中
     */
    private void showIndexIndicator(boolean init) {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            if (init) indexIndicator.attach(this);
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

    /**
     * 在 TransferImage 面板中添加显示自定义 View
     */
    private void showCustomView(boolean init) {
        View customView = transConfig.getCustomView();
        if (customView != null) {
            if (init) addView(customView);
            customView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏自定义 View
     */
    private void hideCustomView() {
        View customView = transConfig.getCustomView();
        if (customView != null) {
            customView.setVisibility(GONE);
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
