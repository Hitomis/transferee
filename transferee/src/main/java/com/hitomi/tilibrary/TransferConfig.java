package com.hitomi.tilibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.style.ITransferAnimator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Attributes <br/>
 * <p>
 * Created by hitomi on 2017/1/19.
 */
public class TransferConfig {

    /**
     * 高清图尚未加载，使用原 ImageView 中显示的图片作为缩略图。同时使用 {@link TransferImage#CATE_ANIMA_APART} 动画类型展示图片
     */
    static final int MODE_EMPTY_THUMBNAIL = 1;
    /**
     * 高清图图片已经加载过了，使用高清图作为缩略图。同时使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
     */
    static final int MODE_LOCAL_THUMBNAIL = 1 << 1;
    /**
     * 用户指定了缩略图路径，使用该路径加载缩略图，并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
     */
    static final int MODE_REMOTE_THUMBNAIL = 1 << 2;

    static final String SP_FILE = "transferee";
    static final String SP_LOAD_SET = "load_set";

    private int nowThumbnailIndex;
    private int offscreenPageLimit;
    private int missPlaceHolder;
    private int errorPlaceHolder;
    private long duration;

    private Drawable missDrawable;
    private Drawable errorDrawable;

    private List<ImageView> originImageList;
    private List<String> sourceImageList;
    private List<String> thumbnailImageList;

    private ITransferAnimator transferAnima;
    private IProgressIndicator progressIndicator;
    private IIndexIndicator indexIndicator;
    private ImageLoader imageLoader;

    public static Builder build() {
        return new Builder();
    }

    public int getNowThumbnailIndex() {
        return nowThumbnailIndex;
    }

    public void setNowThumbnailIndex(int nowThumbnailIndex) {
        this.nowThumbnailIndex = nowThumbnailIndex;
    }

    public int getOffscreenPageLimit() {
        return offscreenPageLimit;
    }

    public void setOffscreenPageLimit(int offscreenPageLimit) {
        this.offscreenPageLimit = offscreenPageLimit;
    }

    public int getMissPlaceHolder() {
        return missPlaceHolder;
    }

    public void setMissPlaceHolder(int missPlaceHolder) {
        this.missPlaceHolder = missPlaceHolder;
    }

    public int getErrorPlaceHolder() {
        return errorPlaceHolder;
    }

    public void setErrorPlaceHolder(int errorPlaceHolder) {
        this.errorPlaceHolder = errorPlaceHolder;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Drawable getMissDrawable(Context context) {
        if (missDrawable == null && missPlaceHolder != 0)
            return context.getResources().getDrawable(missPlaceHolder);
        return missDrawable;
    }

    public void setMissDrawable(Drawable missDrawable) {
        this.missDrawable = missDrawable;
    }

    public Drawable getErrorDrawable(Context context) {
        if (errorDrawable == null && errorPlaceHolder != 0)
            return context.getResources().getDrawable(errorPlaceHolder);
        return errorDrawable;
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
    }

    public List<ImageView> getOriginImageList() {
        return originImageList;
    }

    public void setOriginImageList(List<ImageView> originImageList) {
        this.originImageList = originImageList;
    }

    public List<String> getSourceImageList() {
        return sourceImageList;
    }

    public void setSourceImageList(List<String> sourceImageList) {
        this.sourceImageList = sourceImageList;
    }

    public List<String> getThumbnailImageList() {
        return thumbnailImageList;
    }

    public void setThumbnailImageList(List<String> thumbnailImageList) {
        this.thumbnailImageList = thumbnailImageList;
    }

    public ITransferAnimator getTransferAnima() {
        return transferAnima;
    }

    public void setTransferAnima(ITransferAnimator transferAnima) {
        this.transferAnima = transferAnima;
    }

    public IProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(IProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public IIndexIndicator getIndexIndicator() {
        return indexIndicator;
    }

    public void setIndexIndicator(IIndexIndicator indexIndicator) {
        this.indexIndicator = indexIndicator;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    /**
     * 原图路径集合是否为空
     *
     * @return true : 空
     */
    public boolean isSourceEmpty() {
        return sourceImageList == null || sourceImageList.isEmpty();
    }

    /**
     * 缩略图路径集合是否为空
     *
     * @return true : 空
     */
    public boolean isThumbnailEmpty() {
        return thumbnailImageList == null || thumbnailImageList.isEmpty();
    }

    /**
     * 获取当前在 ViewPager 中待加载（显示）的原图路径
     *
     * @return 原图路径
     */
    public String getNowSourceImageUrl() {
        return sourceImageList.get(nowThumbnailIndex);
    }

    /**
     * 获取当前待加载（显示）的缩略图路径
     *
     * @return 缩略图路径
     */
    public String getNowThumbnailImageUrl() {
        return thumbnailImageList.get(nowThumbnailIndex);
    }

    /**
     * 获取缩略图状态
     *
     * @param context    上下文环境
     * @param thumbIndex 当前在 ViewPager 中待加载（显示）的缩略图索引
     * @return {@link #MODE_EMPTY_THUMBNAIL}, {@link #MODE_LOCAL_THUMBNAIL}, {@link #MODE_REMOTE_THUMBNAIL}
     */
    public int getThumbMode(Context context, int thumbIndex) {
        int thumbMode;

        if (!isThumbnailEmpty()) { // 客户端指定了缩略图路径集合
            thumbMode = MODE_REMOTE_THUMBNAIL;
        } else {
            String url = sourceImageList.get(thumbIndex);
            if (containsSourceImageUrl(context, url)) {
                thumbMode = MODE_LOCAL_THUMBNAIL;
            } else {
                thumbMode = MODE_EMPTY_THUMBNAIL;
            }
        }

        return thumbMode;
    }

    public boolean containsSourceImageUrl(Context context, String url) {
        SharedPreferences loadSharedPref = context.getSharedPreferences(
                SP_FILE, Context.MODE_PRIVATE);
        Set<String> loadedSet = loadSharedPref.getStringSet(SP_LOAD_SET, new HashSet<String>());
        return loadedSet.contains(url);

    }

    public void cacheLoadedImageUrl(final Context context, final String url) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                SharedPreferences loadSharedPref = context.getSharedPreferences(
                        SP_FILE, Context.MODE_PRIVATE);
                Set<String> loadedSet = loadSharedPref.getStringSet(SP_LOAD_SET, new HashSet<String>());
                if (!loadedSet.contains(url)) {
                    loadedSet.add(url);

                    loadSharedPref.edit()
                            .clear() // SharedPreferences 关于 putStringSet 的 bug 修复方案
                            .putStringSet(SP_LOAD_SET, loadedSet)
                            .apply();
                }
            }
        });
    }

    public static class Builder {
        private int nowThumbnailIndex;
        private int offscreenPageLimit;
        private int missPlaceHolder;
        private int errorPlaceHolder;
        private long duration;

        private Drawable missDrawable;
        private Drawable errorDrawable;

        private List<ImageView> originImageList;
        private List<String> sourceImageList;
        private List<String> thumbnailImageList;

        private ITransferAnimator transferAnima;
        private IProgressIndicator progressIndicator;
        private IIndexIndicator indexIndicator;
        private ImageLoader imageLoader;

        /**
         * 当前缩略图在所有图片中的索引
         */
        public Builder setNowThumbnailIndex(int nowThumbnailIndex) {
            this.nowThumbnailIndex = nowThumbnailIndex;
            return this;
        }

        /**
         * <p>ViewPager 中进行初始化并保留的页面设置为当前页面加上当前页面两侧的页面</p>
         * <p>默认为1, 表示第一次加载3张(nowThumbnailIndex, nowThumbnailIndex
         * + 1, nowThumbnailIndex - 1);值为 2, 表示加载5张。依次类推</p>
         * <p>这个参数是为了优化而提供的，值越大，初始化创建的页面越多，保留的页面也
         * 越多，推荐使用默认值</p>
         */
        public Builder setOffscreenPageLimit(int offscreenPageLimit) {
            this.offscreenPageLimit = offscreenPageLimit;
            return this;
        }

        /**
         * 缺省的占位图(资源ID)
         */
        public Builder setMissPlaceHolder(int missPlaceHolder) {
            this.missPlaceHolder = missPlaceHolder;
            return this;
        }

        /**
         * 图片加载错误显示的图片(资源ID)
         */
        public Builder setErrorPlaceHolder(int errorPlaceHolder) {
            this.errorPlaceHolder = errorPlaceHolder;
            return this;
        }

        /**
         * 动画播放时长
         */
        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * 缺省的占位图(Drawable 格式)
         */
        public Builder setMissDrawable(Drawable missDrawable) {
            this.missDrawable = missDrawable;
            return this;
        }

        /**
         * 图片加载错误显示的图片(Drawable 格式)
         */
        public Builder setErrorDrawable(Drawable errorDrawable) {
            this.errorDrawable = errorDrawable;
            return this;
        }

        /**
         * 原始的 ImageView 集合
         */
        public Builder setOriginImageList(List<ImageView> originImageList) {
            this.originImageList = originImageList;
            return this;
        }

        /**
         * 高清图的地址集合
         */
        public Builder setSourceImageList(List<String> sourceImageList) {
            this.sourceImageList = sourceImageList;
            return this;
        }

        /**
         * 缩略图地址集合
         */
        public Builder setThumbnailImageList(List<String> thumbnailImageList) {
            this.thumbnailImageList = thumbnailImageList;
            return this;
        }

        /**
         * 扩展动画
         */
        public Builder setTransferAnima(ITransferAnimator transferAnima) {
            this.transferAnima = transferAnima;
            return this;
        }

        /**
         * 加载高清图的进度条 (默认内置 ProgressPieIndicator), 可自实现
         * IProgressIndicator 接口定义自己的图片加载进度条
         */
        public Builder setProgressIndicator(IProgressIndicator progressIndicator) {
            this.progressIndicator = progressIndicator;
            return this;
        }

        /**
         * 图片索引指示器 (默认内置 IndexCircleIndicator), 可自实现
         * IIndexIndicator 接口定义自己的图片索引指示器
         */
        public Builder setIndexIndicator(IIndexIndicator indexIndicator) {
            this.indexIndicator = indexIndicator;
            return this;
        }

        /**
         * 图片加载器 (默认内置 GlideImageLoader), 可自实现
         * ImageLoader 接口定义自己的图片加载器
         */
        public Builder setImageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            return this;
        }

        public TransferConfig create() {
            TransferConfig config = new TransferConfig();

            config.setNowThumbnailIndex(nowThumbnailIndex);
            config.setOffscreenPageLimit(offscreenPageLimit);
            config.setMissPlaceHolder(missPlaceHolder);
            config.setErrorPlaceHolder(errorPlaceHolder);
            config.setDuration(duration);

            config.setMissDrawable(missDrawable);
            config.setErrorDrawable(errorDrawable);

            config.setOriginImageList(originImageList);
            config.setSourceImageList(sourceImageList);
            config.setThumbnailImageList(thumbnailImageList);

            config.setTransferAnima(transferAnima);
            config.setProgressIndicator(progressIndicator);
            config.setIndexIndicator(indexIndicator);
            config.setImageLoader(imageLoader);

            return config;
        }
    }
}
