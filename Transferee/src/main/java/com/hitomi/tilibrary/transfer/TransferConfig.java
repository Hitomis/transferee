package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Transferee Attributes
 * <p>
 * Created by hitomi on 2017/1/19.
 * <p>
 * email: 196425254@qq.com
 */
public final class TransferConfig {
    private int nowThumbnailIndex;
    private int offscreenPageLimit;
    private int missPlaceHolder;
    private int errorPlaceHolder;
    private int backgroundColor;
    private long duration;
    private boolean justLoadHitImage;

    private Drawable missDrawable;
    private Drawable errorDrawable;

    private List<ImageView> originImageList;
    private List<String> sourceImageList;
    private List<String> thumbnailImageList;

    private IProgressIndicator progressIndicator;
    private IIndexIndicator indexIndicator;
    private ImageLoader imageLoader;

    private @IdRes int imageId;
    private AbsListView listView;
    private RecyclerView recyclerView;

    private Transferee.OnTransfereeLongClickListener longClickListener;

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

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isJustLoadHitImage() {
        return justLoadHitImage;
    }

    public void setJustLoadHitImage(boolean justLoadHitImage) {
        this.justLoadHitImage = justLoadHitImage;
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
        return originImageList == null ? new ArrayList<ImageView>() : originImageList;
    }

    void setOriginImageList(List<ImageView> originImageList) {
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

    public Transferee.OnTransfereeLongClickListener getLongClickListener() {
        return longClickListener;
    }

    public void setLongClickListener(Transferee.OnTransfereeLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public AbsListView getListView() {
        return listView;
    }

    public void setListView(AbsListView listView) {
        this.listView = listView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public static class Builder {
        private int nowThumbnailIndex;
        private int offscreenPageLimit;
        private int missPlaceHolder;
        private int errorPlaceHolder;
        private int backgroundColor;
        private long duration;
        private boolean justLoadHitImage;

        private Drawable missDrawable;
        private Drawable errorDrawable;

        private List<String> sourceImageList;
        private List<String> thumbnailImageList;

        private IProgressIndicator progressIndicator;
        private IIndexIndicator indexIndicator;
        private ImageLoader imageLoader;

        private @IdRes int imageId;
        private AbsListView listView;
        private RecyclerView recyclerView;

        private Transferee.OnTransfereeLongClickListener longClickListener;

        /**
         * 当前缩略图在所有图片中的索引
         */
        public Builder setNowThumbnailIndex(int nowThumbnailIndex) {
            this.nowThumbnailIndex = nowThumbnailIndex;
            return this;
        }

        /**
         * <p>ViewPager 中进行初始化并创建的页面 : 设置为当前页面加上当前页面两侧的页面</p>
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
         * 为 transferee 组件设置背景颜色
         */
        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
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
         * 仅仅只加载当前显示的图片
         */
        public Builder setJustLoadHitImage(boolean justLoadHitImage) {
            this.justLoadHitImage = justLoadHitImage;
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

        /**
         * 绑定 transferee 长按操作监听器
         */
        public Builder setOnLongClcikListener(Transferee.OnTransfereeLongClickListener listener) {
            this.longClickListener = listener;
            return this;
        }

        public Builder setImageId(int imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder setListView(AbsListView listView) {
            this.listView = listView;
            return this;
        }

        public Builder setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return this;
        }

        public TransferConfig create() {
            TransferConfig config = new TransferConfig();

            config.setNowThumbnailIndex(nowThumbnailIndex);
            config.setOffscreenPageLimit(offscreenPageLimit);
            config.setMissPlaceHolder(missPlaceHolder);
            config.setErrorPlaceHolder(errorPlaceHolder);
            config.setBackgroundColor(backgroundColor);
            config.setDuration(duration);
            config.setJustLoadHitImage(justLoadHitImage);

            config.setMissDrawable(missDrawable);
            config.setErrorDrawable(errorDrawable);

            config.setSourceImageList(sourceImageList);
            config.setThumbnailImageList(thumbnailImageList);

            config.setProgressIndicator(progressIndicator);
            config.setIndexIndicator(indexIndicator);
            config.setImageLoader(imageLoader);

            config.setImageId(imageId);
            config.setListView(listView);
            config.setRecyclerView(recyclerView);

            config.setLongClickListener(longClickListener);

            return config;
        }
    }
}
