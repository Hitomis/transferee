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
import com.hitomi.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.style.index.IndexCircleIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;
import com.hitomi.tilibrary.view.fleximage.FlexImageView;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * Main workflow: <br/>
 * 1、点击缩略图展示缩略图到 TransferImage 过渡动画 <br/>
 * 2、显示下载高清图片进度 <br/>
 * 3、加载完成显示高清图片 <br/>
 * 4、高清图支持手势缩放 <br/>
 * 5、关闭 TransferImage 展示 TransferImage 到原缩略图的过渡动画 <br/>
 * Created by hitomi on 2017/1/19.
 */
public class TransferImage extends FrameLayout {

    static volatile TransferImage defaultInstance;

    private Context context;
    private FlexImageView sharedImage;

    private ViewPager transViewPager;
    private TransferAdapter transAdapter;
    private TransferConfig transConfig;

    private Set<Integer> loadedIndexSet;
    private boolean shown;

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
                    dismiss();
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
            setOriginImageVisibility(View.INVISIBLE);
            createSharedImage(pos, FlexImageView.STATE_TRANS_OUT);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    transViewPager.setVisibility(View.INVISIBLE);
                }
            }, sharedImage.getDuration() / 2);
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
     * 构造方法私有化，通过{@link #getDefault(Context)} 创建 TransferImage
     *
     * @param context 上下文环境
     */
    private TransferImage(Context context) {
        super(context);
        this.context = context;
        this.loadedIndexSet = new HashSet<>();
    }

    public static TransferImage getDefault(Context context) {
        if (defaultInstance == null) {
            synchronized (TransferImage.class) {
                if (defaultInstance == null) {
                    defaultInstance = new TransferImage(context);
                }
            }
        }
        return defaultInstance;
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
            vg.removeView(TransferImage.this);
        }
    }

    private void initTransfer() {
        createTransferViewPager();
        createSharedImage(transConfig.getNowThumbnailIndex(),
                FlexImageView.STATE_TRANS_IN);
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
    private void createSharedImage(int pos, int state) {
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

        switch (state) {
            case FlexImageView.STATE_TRANS_IN:
                sharedImage.transformIn();
                break;
            case FlexImageView.STATE_TRANS_OUT:
                sharedImage.transformOut();
                break;
        }

        String sharedUrl = transConfig.getSourceImageList().get(transConfig.getNowThumbnailIndex());
        transConfig.getImageLoader().displayThumbnailImage(sharedUrl, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                sharedImage.setImageDrawable(drawable);
            }
        });
        addView(sharedImage);
    }

    private void removeFromParent(View view) {
        ViewGroup vg = (ViewGroup) view.getParent();
        if (vg != null)
            vg.removeView(view);
    }

    private void checkConfig() {
        transConfig.setNowThumbnailIndex(transConfig.getNowThumbnailIndex() < 0
                ? 0 : transConfig.getNowThumbnailIndex());

        transConfig.setBackgroundColor(transConfig.getBackgroundColor() == 0
                ? Color.BLACK : transConfig.getBackgroundColor());

        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());

        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressPieIndicator() : transConfig.getProgressIndicator());

        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new IndexCircleIndicator() : transConfig.getIndexIndicator());

        transConfig.setImageLoader(transConfig.getImageLoader() == null
                ? GlideImageLoader.with(context) : transConfig.getImageLoader());

        if (transConfig.getSourceImageList() == null || transConfig.getSourceImageList().isEmpty())
            transConfig.setSourceImageList(transConfig.getThumbnailImageList());

        if (transConfig.getThumbnailImageList() == null || transConfig.getThumbnailImageList().isEmpty())
            transConfig.setThumbnailImageList(transConfig.getSourceImageList());

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // unregister PageChangeListener
        transViewPager.removeOnPageChangeListener(transChangeListener);
    }

    /**
     * TransferImage 是否显示
     *
     * @return true ：显示, false ：关闭
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * 显示 TransferImage
     */
    public void show() {
        if (!shown) {
            shown = true;
            checkConfig();
            addToWindow();
            initTransfer();
        }
    }

    public TransferImage apply(TransferConfig config) {
        transConfig = config;
        return defaultInstance;
    }

    /**
     * 关闭 TransferImage
     */
    public void dismiss() {
        if (!shown) return;
        shown = false;

        loadedIndexSet.clear();
        removeIndexIndicator();
        removeAllViews();
        removeFromWindow();

    }

    public void destroy() {
        defaultInstance = null;
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
