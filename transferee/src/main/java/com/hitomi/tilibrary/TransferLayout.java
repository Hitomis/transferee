package com.hitomi.tilibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
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
    static final String SP_FILE = "transferee";
    static final String SP_LOAD_SET = "load_set";

    /**
     * 高清图图片已经加载过了，直接使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
     */
    static final int MODE_LOCAL_THUMBNAIL = 1;
    /**
     * 高清图尚未加载，使用 {@link TransferImage#CATE_ANIMA_APART} 动画类型展示图片
     */
    static final int MODE_EMPTY_THUMBNAIL = 1 << 1;
    /**
     * 用户指定了缩略图路径，使用该路径加载缩略图，并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
     */
    static final int MODE_REMOTE_THUMBNAIL = 1 << 2;

    private Context context;

    private TransferImage transImage;
    private ViewPager transViewPager;
    private TransferAdapter transAdapter;
    private TransferConfig transConfig;

    private OnLayoutResetListener layoutResetListener;
    private SharedPreferences loadSharedPref;

    private Set<Integer> loadedIndexSet;
    private SparseBooleanArray loadedArray;
    private int thumbMode;

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
    private TransferImage.OnTransferListener transferListener = new TransferImage.OnTransferListener() {
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
        this.loadedArray = new SparseBooleanArray();

        loadSharedPref = context.getSharedPreferences(
                SP_FILE, Context.MODE_PRIVATE);
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
        switch (thumbMode) {
            case MODE_EMPTY_THUMBNAIL:
                loadSourceImageByEmptyThumbnail(position);
                break;
            case MODE_LOCAL_THUMBNAIL:
                loadSourceImageByLoadedThumbnail(position);
                break;
            case MODE_REMOTE_THUMBNAIL:
                loadSourceImageByLoadedThumbnail(position);
                break;
        }
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
        transAdapter = new TransferAdapter(transConfig, thumbMode);
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

    private void createTransferImage() {
        ImageView originImage = transConfig.getOriginImageList().get(
                transConfig.getNowThumbnailIndex());
        int[] location = new int[2];
        originImage.getLocationInWindow(location);

        transImage = new TransferImage(context);
        transImage.setScaleType(FIT_CENTER);
        transImage.setImageDrawable(originImage.getDrawable());
        transImage.setOriginalInfo(location[0], getTransImageLocalY(location[1]),
                originImage.getWidth(), originImage.getHeight());
        transImage.setDuration(transConfig.getDuration());
        transImage.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        transImage.setOnTransferListener(transferListener);
        transImage.transformIn(TransferImage.STAGE_TRANSLATE);

        addView(transImage, 1);
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
        transImage.setOriginalInfo(location[0], getTransImageLocalY(location[1]),
                originImage.getWidth(), originImage.getHeight());
        transImage.setDuration(transConfig.getDuration());
        transImage.setLayoutParams(new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        transImage.setOnTransferListener(transferListener);
        addView(transImage, 1);

        if (thumbMode == MODE_EMPTY_THUMBNAIL) {
            Drawable thumbnailDrawable = transAdapter.getImageItem(
                    transConfig.getNowThumbnailIndex()).getDrawable();
            transImage.setImageDrawable(thumbnailDrawable);
            if (!loadedArray.get(pos)) { // 还在加载中 -> 当前显示的是小缩略图
                transImage.transformOut(TransferImage.STAGE_TRANSLATE);
            } else {
                switch (state) {
                    case TransferImage.STATE_TRANS_IN:
                        transImage.transformIn();
                        break;
                    case TransferImage.STATE_TRANS_OUT:
                        transImage.transformOut();
                        break;
                }
            }

        } else if (thumbMode == MODE_REMOTE_THUMBNAIL || thumbMode == MODE_LOCAL_THUMBNAIL) {

            String transUrl;
            if (thumbMode == MODE_REMOTE_THUMBNAIL) {
                transUrl = transConfig.getNowThumbnailImageUrl();
            } else {
                transUrl = transConfig.getNowSourceImageUrl();
            }

            transConfig.getImageLoader().loadThumbnailAsync(transUrl, transImage, new ImageLoader.ThumbnailCallback() {
                @Override
                public void onFinish(Drawable drawable) {
                    transImage.setImageDrawable(drawable);

                    switch (state) {
                        case TransferImage.STATE_TRANS_IN:
                            transImage.transformIn();
                            break;
                        case TransferImage.STATE_TRANS_OUT:
                            transImage.transformOut();
                            break;
                    }
                }
            });
        }
    }

    private int getTransImageLocalY(int oldY) {
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
        updateThumbMode();
        createTransferViewPager();

        switch (thumbMode) {
            case MODE_LOCAL_THUMBNAIL:
                createTransferImage(transConfig.getNowThumbnailIndex(),
                        TransferImage.STATE_TRANS_IN);
                break;
            case MODE_EMPTY_THUMBNAIL:
                createTransferImage();
                break;
            case MODE_REMOTE_THUMBNAIL:
                createTransferImage(transConfig.getNowThumbnailIndex(),
                        TransferImage.STATE_TRANS_IN);
                break;
        }

    }

    private void updateThumbMode() {
        String url = transConfig.getNowSourceImageUrl();
        if (containsSourceImageUrl(url)) {
            thumbMode = MODE_LOCAL_THUMBNAIL;
        } else {
            if (transConfig.isThumbnailEmpty()) {
                thumbMode = MODE_EMPTY_THUMBNAIL;
            } else {
                thumbMode = MODE_REMOTE_THUMBNAIL;
            }
        }
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

    private void bindOnDismissListener(ImageView imageView, final int pos) {
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(pos);
            }
        });
    }

    /**
     * 加载高清图(高清图未缓存，使用原缩略图作为占位图)
     *
     * @param position 当前加载的图片索引
     */
    private void loadSourceImageByEmptyThumbnail(final int position) {
        final String imgUrl = transConfig.getSourceImageList().get(position);
        final TransferImage targetImage = transAdapter.getImageItem(position);
        ImageView originImage = transConfig.getOriginImageList().get(position);

        final IProgressIndicator progressIndicator = transConfig.getProgressIndicator();
        progressIndicator.attach(position, transAdapter.getParentItem(position));

        transConfig.getImageLoader().showSourceImage(imgUrl, targetImage,
                originImage.getDrawable(), new ImageLoader.SourceCallback() {

                    @Override
                    public void onStart() {
                        progressIndicator.onStart(position);
                    }

                    @Override
                    public void onProgress(int progress) {
                        progressIndicator.onProgress(position, progress);
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDelivered(int status) {
                        switch (status) {
                            case ImageLoader.STATUS_DISPLAY_SUCCESS: // 加载成功
                                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像

                                cacheLoadedImageUrl(imgUrl);
                                targetImage.transformIn(TransferImage.STAGE_SCALE);
                                targetImage.enable();
                                bindOnDismissListener(targetImage, position);
                                loadedArray.put(position, true);
                                break;
                            case ImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                                targetImage.setImageDrawable(transConfig.getErrorDrawable(context));
                                break;
                        }
                    }
                });

    }

    /**
     * 加载高清图（高清图已缓存，直接使用高清图作为占位图）
     *
     * @param position 当前加载的图片索引
     */
    private void loadSourceImageByLoadedThumbnail(final int position) {
        final String imgUrl = transConfig.getSourceImageList().get(position);
        final TransferImage targetImage = transAdapter.getImageItem(position);
        final ImageLoader imageLoader = transConfig.getImageLoader();

        imageLoader.loadThumbnailAsync(imgUrl, targetImage, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                imageLoader.showSourceImage(imgUrl, targetImage,
                        drawable, new ImageLoader.SourceCallback() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onProgress(int progress) {
                            }

                            @Override
                            public void onFinish() {
                            }

                            @Override
                            public void onDelivered(int status) {
                                switch (status) {
                                    case ImageLoader.STATUS_DISPLAY_SUCCESS: // 加载成功，启用 TransferImage 的手势缩放功能
                                        cacheLoadedImageUrl(imgUrl);
                                        targetImage.enable();
                                        bindOnDismissListener(targetImage, position);
                                        break;
                                    case ImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                                        targetImage.setImageDrawable(transConfig.getErrorDrawable(context));
                                        break;
                                }
                            }
                        });
            }
        });
    }

    private void cacheLoadedImageUrl(final String url) {
        Set<String> loadedSet = loadSharedPref.getStringSet(SP_LOAD_SET, new HashSet<String>());
        if (!loadedSet.contains(url)) {
            loadedSet.add(url);

            loadSharedPref.edit()
                    .clear() // SharedPreferences 关于 putStringSet 的 bug 修复方案
                    .putStringSet(SP_LOAD_SET, loadedSet)
                    .apply();
        }
    }

    private boolean containsSourceImageUrl(String url) {
        Set<String> loadedSet = loadSharedPref.getStringSet(SP_LOAD_SET, new HashSet<String>());
        return loadedSet.contains(url);

    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
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
