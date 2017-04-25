package com.hitomi.tilibrary;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.PhotoView;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

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

    private Set<Integer> loadedIndexSet;

    private OnLayoutResetListener layoutResetListener;

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            setOriginImageVisibility(View.VISIBLE); // 显示出之前的缩略图
            transConfig.setNowThumbnailIndex(position);
            setOriginImageVisibility(View.INVISIBLE); // 隐藏当前的缩略图

            if (!loadedIndexSet.contains(position)) {
                loadSourceImage(position);
                loadedIndexSet.add(position);
            }

            for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                int left = position - i;
                int right = position + i;
                if (left >= 0 && !loadedIndexSet.contains(left)) {
                    loadSourceImage(left);
                    loadedIndexSet.add(left);
                }
                if (right < transConfig.getSourceImageList().size() && !loadedIndexSet.contains(right)) {
                    loadSourceImage(right);
                    loadedIndexSet.add(right);
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

            // 初始加载第一张原图
            int position = transConfig.getNowThumbnailIndex();
            loadSourceImage(position);
            loadedIndexSet.add(position);
        }
    };

    /**
     * TransferImage 伸/缩动画执行完成监听器
     */
    private TransferImage.OnTransferListener transferListener = new TransferImage.OnTransferListener() {
        @Override
        public void onTransferComplete(int mode) {
            switch (mode) {
                case TransferImage.STATE_TRANS_IN: // 伸展动画执行完毕
                    addIndexIndicator();
                    transViewPager.setVisibility(View.VISIBLE);
                    removeFromParent(transImage);
                    break;
                case TransferImage.STATE_TRANS_OUT: // 缩小动画执行完毕
                    setOriginImageVisibility(View.VISIBLE);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetTransfer();
                        }
                    }, 10);
                    break;
            }

        }
    };
    /**
     * 点击 ImageView 关闭 TransferImage 的监听器
     */
    private TransferAdapter.OnDismissListener dismissListener = new TransferAdapter.OnDismissListener() {
        @Override
        public void onDismiss(final int pos) {
            if (transImage != null &&
                    transImage.getState() == TransferImage.STATE_TRANS_OUT)
                return;

            dismiss(pos);
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
     * 初始化 TransferImage
     */
    private void initTransfer() {
        createTransferViewPager();
        if (transConfig.isThumbnailEmpty()) {

//            ImageView originImage = transConfig.getOriginImageList().get(
//                    transConfig.getNowThumbnailIndex());
//            int[] location = new int[2];
//            originImage.getLocationInWindow(location);
//
//            transImage = new TransferImage(context);
//            transImage.setScaleType(CENTER_CROP);
//            transImage.setImageDrawable(originImage.getDrawable());
//            transImage.setOriginalInfo(originImage.getWidth(),
//                    originImage.getHeight(), location[0], getTransImageLocalY(location[1]));
//            transImage.setDuration(transConfig.getDuration());
//            transImage.setLayoutParams(new FrameLayout.LayoutParams(
//                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//            transImage.setOnTransferListener(transferListener);
//            addView(transImage, 1);
//            transImage.transformIn();

        } else {
            createTransferImage(transConfig.getNowThumbnailIndex(),
                    TransferImage.STATE_TRANS_IN);
        }
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
        transAdapter = new TransferAdapter(transConfig.getNowThumbnailIndex(),
                transConfig.getSourceImageList().size(),
                transConfig.getMissDrawable(context));
        transAdapter.setOnDismissListener(dismissListener);
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

    /**
     * 创建 TransferImage 模拟图片扩大的过渡动画
     */
    private void createTransferImage(final int pos, final int state) {
        ImageView originImage = transConfig.getOriginImageList().get(pos);
        int[] location = new int[2];
        originImage.getLocationInWindow(location);

        transImage = new TransferImage(context);
        transImage.setScaleType(FIT_CENTER);
        transImage.setOriginalInfo(originImage.getWidth(),
                originImage.getHeight(), location[0], getTransImageLocalY(location[1]));
        transImage.setDuration(transConfig.getDuration());
        transImage.setLayoutParams(new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        transImage.setOnTransferListener(transferListener);
        addView(transImage, 1);

        String transUrl = transConfig.getThumbnailImageList().get(transConfig.getNowThumbnailIndex());
        transConfig.getImageLoader().displayThumbnailImageAsync(transUrl, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                transImage.setImageDrawable(drawable);

                switch (state) {
                    case TransferImage.STATE_TRANS_IN:
                        transImage.transformIn();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setOriginImageVisibility(View.INVISIBLE);
                            }
                        }, 10);
                        break;
                    case TransferImage.STATE_TRANS_OUT:
                        transImage.transformOut();
                        break;
                }
            }
        });
    }

    private int getTransImageLocalY(int oldY){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return oldY;
        }
        return oldY - getStatusBarHeight();
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

    /**
     * 初始化 TransferLayout 中的各个组件，并显示，同时开启动画
     */
    public void show() {
        initTransfer();
    }

    /**
     * 开启 transferImage 关闭动画，并隐藏 transferLayout 中的各个组件
     *
     * @param pos
     */
    public void dismiss(int pos) {
        createTransferImage(pos, TransferImage.STATE_TRANS_OUT);
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

    /**
     * 加载高清图
     *
     * @param position 图片下标
     */
    private void loadSourceImage(final int position) {
        final String imgUrl = transConfig.getSourceImageList().get(position);
        transConfig.getImageLoader().displayThumbnailImageAsync(imgUrl, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                transConfig.getImageLoader().displaySourceImage(imgUrl,
                        transAdapter.getImageItem(position), drawable, new ImageLoader.SourceCallback() {

                    private IProgressIndicator progressIndicator = transConfig.getProgressIndicator();

                    @Override
                    public void onStart() {
                        if (progressIndicator == null) return;
                        progressIndicator.attach(position, transAdapter.getParentItem(position));
                        progressIndicator.onStart(position);
                    }

                    @Override
                    public void onProgress(int progress) {
                        if (progressIndicator == null) return;
                        progressIndicator.onProgress(position, progress);
                    }

                    @Override
                    public void onFinish() {
                        if (progressIndicator == null) return;
                        progressIndicator.onFinish(position);
                        transAdapter.getImageItem(position).enable();
                    }

                    @Override
                    public void onDelivered(int status) {
                        PhotoView photoView = transAdapter.getImageItem(position);
                        if (status == ImageLoader.STATUS_DISPLAY_SUCCESS)
                            // 加载成功，启用 PhotoView 的缩放功能
                            photoView.enable();
                        else if (status == ImageLoader.STATUS_DISPLAY_FAILED)
                            // 加载失败，显示加载错误的占位图
                            photoView.setImageDrawable(transConfig.getErrorDrawable(context));
                    }
                });
            }
        });
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
