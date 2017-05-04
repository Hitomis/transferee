package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * TransferImage 中 Dialog 显示的内容
 * Created by Hitomis on 2017/4/23 0023.
 */
class TransferLayout extends FrameLayout {
    private Context context;

    private TransferImage transImage;
    private ViewPager transViewPager;
    private TransferAdapter transAdapter;
    private TransferConfig transConfig;

    private OnLayoutResetListener layoutResetListener;
    private Set<Integer> loadedIndexSet;

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
//            setOriginImageVisibility(View.VISIBLE); // 显示出之前的缩略图
            transConfig.setNowThumbnailIndex(position);
//            setOriginImageVisibility(View.INVISIBLE); // 隐藏当前的缩略图

            for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                loadSourceImageOffset(position, i);
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
            loadSourceImageOffset(position, 1);
        }
    };
    /**
     * TransferImage 伸/缩动画执行完成监听器
     */
    private TransferImage.OnTransferListener transListener = new TransferImage.OnTransferListener() {
        @Override
        public void onTransferComplete(int state, int cate, int stage) {
            if (cate == TransferImage.CATE_ANIMA_TOGETHER) {
                switch (state) {
                    case TransferImage.STATE_TRANS_IN: // 伸展动画执行完毕
                        addIndexIndicator();
                        transViewPager.setVisibility(View.VISIBLE);
                        removeFromParent(transImage);
                        break;
                    case TransferImage.STATE_TRANS_OUT: // 缩小动画执行完毕
                        resetTransfer();
                        break;
                }
            } else { // 如果动画是分离的
                switch (state) {
                    case TransferImage.STATE_TRANS_IN:
                        if (stage == TransferImage.STAGE_TRANSLATE) {
                            // 第一阶段位移动画执行完毕
                            addIndexIndicator();
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

    private void loadSourceImage(int position) {
        BaseTransferState transferState = getTransferState(position);
        transferState.loadTransfer(position);
    }

    /**
     * 设置当前显示大图对应的缩略图隐藏或者显示
     *
     * @param visibility
     */
    private void setOriginImageVisibility(int visibility) {
        int showIndex = transConfig.getNowThumbnailIndex();
        ImageView originImage = transConfig.getOriginImageList().get(showIndex);
        originImage.setVisibility(visibility);
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
     * 创建 ViewPager
     */
    private void createTransferViewPager() {
        transAdapter = new TransferAdapter(transConfig);
        transAdapter.setOnInstantListener(instantListener);

        transViewPager = new ViewPager(context);
        // 先隐藏，待 ViewPager 下标为 config.getCurrOriginIndex() 的页面创建完毕再显示
        transViewPager.setVisibility(View.INVISIBLE);
        transViewPager.setBackgroundColor(Color.BLACK);
        transViewPager.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() + 1);
        transViewPager.setAdapter(transAdapter);
        transViewPager.setCurrentItem(transConfig.getNowThumbnailIndex());

        addView(transViewPager, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

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

    public TransferAdapter getTransAdapter() {
        return transAdapter;
    }

    public TransferConfig getTransConfig() {
        return transConfig;
    }

    public TransferImage.OnTransferListener getTransListener() {
        return transListener;
    }

    /**
     * 初始化 TransferLayout 中的各个组件，并显示，同时开启动画
     */
    public void show() {
        createTransferViewPager();

        int nowThumbnailIndex = transConfig.getNowThumbnailIndex();
        BaseTransferState transferState = getTransferState(nowThumbnailIndex);
        transImage = transferState.createTransferIn(nowThumbnailIndex);

    }

    private BaseTransferState getTransferState(int position) {
        BaseTransferState transferState;

        if (!transConfig.isThumbnailEmpty()) { // 客户端指定了缩略图路径集合
            transferState = new RemoteThumState(this);
        } else {
            String url = transConfig.getSourceImageList().get(position);

            if (transConfig.containsSourceImageUrl(context, url)) {
                transferState = new LocalThumState(this);
            } else {
                transferState = new EmptyThumState(this);
            }
        }

        return transferState;
    }

    /**
     * 开启 transferImage 关闭动画，并隐藏 transferLayout 中的各个组件
     *
     * @param pos
     */
    public void dismiss(int pos) {
        if (transImage != null && transImage.getState()
                == TransferImage.STATE_TRANS_OUT) // 防止双击
            return;

        BaseTransferState transferState = getTransferState(pos);
        transImage = transferState.createTransferOut(pos);

        hideIndexIndicator();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                transViewPager.setVisibility(View.INVISIBLE);
            }
        }, transImage.getDuration() / 3);
    }

    /**
     * 配置参数
     *
     * @param config 参数对象
     */
    public void apply(TransferConfig config) {
        transConfig = config;
    }

    /**
     * 绑定 TransferLayout 内容重置时回调监听器
     *
     * @param listener 重置回调监听器
     */
    public void setOnLayoutResetListener(OnLayoutResetListener listener) {
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

    /**
     * 从 TransferImage 面板中移除下标指示器 UI 组件
     */
    private void removeIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.onRemove();
        }
    }

    public void bindOnDismissListener(ImageView imageView, final int pos) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(pos);
            }
        });
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
