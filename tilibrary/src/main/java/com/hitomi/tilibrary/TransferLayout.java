package com.hitomi.tilibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.fleximage.FlexImageView;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
 * Created by Hitomis on 2017/4/23 0023.
 */
class TransferLayout extends FrameLayout {

    private Context context;
    private FlexImageView sharedImage;

    private ViewPager transViewPager;
    private TransferAdapter transAdapter;
    private TransferConfig transConfig;

    private Set<Integer> loadedIndexSet;
    private boolean added;

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            transConfig.setNowThumbnailIndex(position);
            transConfig.setNowShowIndex(position);

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
     * FlexImageView 伸/缩动画执行完成监听器
     */
    private FlexImageView.OnTransferListener transferListener = new FlexImageView.OnTransferListener() {
        @Override
        public void onTransferComplete(int mode) {
            switch (mode) {
                case FlexImageView.STATE_TRANS_IN: // 伸展动画执行完毕
                    addIndexIndicator();
                    transViewPager.setVisibility(View.VISIBLE);
                    removeFromParent(sharedImage);
                    break;
                case FlexImageView.STATE_TRANS_OUT: // 缩小动画执行完毕
                    setOriginImageVisibility(View.VISIBLE);
                    resetTransfer();
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
            dismiss(pos);
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
            transConfig.setNowShowIndex(position);
            loadSourceImage(position);
            loadedIndexSet.add(position);
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
        int showIndex = transConfig.getNowShowIndex();
        ImageView originImage = transConfig.getOriginImageList().get(showIndex);
        originImage.setVisibility(visibility);
    }

    /**
     * 将 TransferImage 添加到 Window 中
     */
    private void addToWindow() {
        FrameLayout.LayoutParams windowLayoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        //(((LinearLayout)(LinearLayout)((ViewGroup) context.getWindow().getDecorView()).getChildAt(0))).getChildAt(0) => 状态栏
        // ((ViewGroup) context.getWindow().getDecorView()) => 状态栏的父布局的父布局

        Activity activity = (Activity) context;
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.addView(this, windowLayoutParams);
//        activity.getWindow().addContentView(this, windowLayoutParams);
    }

    /**
     * 从 Window 中移除 TransferImage
     */
    private void removeFromWindow() {
        ViewGroup vg = (ViewGroup) getParent();
        if (vg != null) {
            vg.removeView(TransferLayout.this);
        }
    }

    /**
     * 初始化 TransferImage
     */
    private void initTransfer() {
        createTransferViewPager();
        createSharedImage(transConfig.getNowThumbnailIndex(),
                FlexImageView.STATE_TRANS_IN);
    }

    /**
     * 重置 TransferImage
     */
    private void resetTransfer() {
        loadedIndexSet.clear();
        removeIndexIndicator();
        removeAllViews();
        removeFromWindow();
        added = false;
    }

    /**
     * 创建 ViewPager
     */
    private void createTransferViewPager() {
        transAdapter = new TransferAdapter(transConfig.getNowThumbnailIndex(), transConfig.getSourceImageList().size());
        transAdapter.setOnDismissListener(dismissListener);
        transAdapter.setOnInstantListener(instantListener);

        transViewPager = new ViewPager(context);
        // 先隐藏，待 ViewPager 下标为 config.getCurrOriginIndex() 的页面创建完毕再显示
        transViewPager.setVisibility(View.INVISIBLE);
        transViewPager.setBackgroundColor(Color.BLACK);
        transViewPager.setOffscreenPageLimit(transConfig.getSourceImageList().size() + 1);
        transViewPager.setAdapter(transAdapter);
        transViewPager.setCurrentItem(transConfig.getNowThumbnailIndex());

        addView(transViewPager, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    /**
     * 创建 SharedImage 模拟图片扩大的过渡动画
     */
    private void createSharedImage(final int pos, final int state) {
        ImageView originImage = transConfig.getOriginImageList().get(pos);
        int[] location = new int[2];
        originImage.getLocationInWindow(location);

        sharedImage = new FlexImageView(context);
        sharedImage.setScaleType(FIT_CENTER);
        sharedImage.setOriginalInfo(originImage.getWidth(),
                originImage.getHeight(), location[0], location[1]);
        sharedImage.setLayoutParams(new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        sharedImage.setOnTransferListener(transferListener);

        String sharedUrl = transConfig.getThumbnailImageList().get(transConfig.getNowThumbnailIndex());
        transConfig.getImageLoader().displayThumbnailImage(sharedUrl, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                sharedImage.setImageDrawable(drawable);
                addView(sharedImage, 1);

                switch (state) {
                    case FlexImageView.STATE_TRANS_IN:
                        sharedImage.transformIn();
                        break;
                    case FlexImageView.STATE_TRANS_OUT:
                        sharedImage.transformOut();
                        break;
                }
            }
        });
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
     * 显示 TransferImage
     */
    public void show() {
        added = true;
        addToWindow();
        initTransfer();
    }

    public void dismiss(int pos) {
        setOriginImageVisibility(View.INVISIBLE);
        createSharedImage(pos, FlexImageView.STATE_TRANS_OUT);
        hideIndexIndicator();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                transViewPager.setVisibility(View.INVISIBLE);
            }
        }, sharedImage.getDuration() / 3);
    }

    public boolean isAdded() {
        return added;
    }

    public void apply(TransferConfig config) {
        transConfig = config;
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
     * @param position
     */
    private void loadSourceImage(final int position) {
        final String imgUrl = transConfig.getSourceImageList().get(position);
        transConfig.getImageLoader().displayThumbnailImage(imgUrl, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                transConfig.getImageLoader().displaySourceImage(imgUrl, transAdapter.getImageItem(position), drawable, new ImageLoader.SourceCallback() {

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
                    }
                });
            }
        });
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度值 unit ：px
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
