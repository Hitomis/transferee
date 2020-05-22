package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

import com.hitomi.tilibrary.R;
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
    private boolean justLoadHitPage;
    private boolean enableDragClose;
    private boolean enableDragHide;
    private boolean enableDragPause;
    private boolean enableAnimateShake;
    private boolean enableScrollingWithPageChange;

    private Drawable missDrawable;
    private Drawable errorDrawable;

    private List<ImageView> originImageList;
    private List<String> sourceImageList;
    private List<Uri> sourceUriList;
    private List<String> thumbnailImageList;

    private IProgressIndicator progressIndicator;
    private IIndexIndicator indexIndicator;
    private ImageLoader imageLoader;

    private @IdRes
    int imageId;
    private ImageView imageView;
    private AbsListView listView;
    private RecyclerView recyclerView;
    private View customView;

    private int headerSize;
    private int footerSize;

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
        return backgroundColor == 0 ? Color.BLACK : backgroundColor;
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

    public boolean isJustLoadHitPage() {
        return justLoadHitPage;
    }

    public void setJustLoadHitPage(boolean justLoadHitPage) {
        this.justLoadHitPage = justLoadHitPage;
    }


    public boolean isEnableDragClose() {
        return enableDragClose;
    }

    public void enableDragClose(boolean enableDragClose) {
        this.enableDragClose = enableDragClose;
    }

    public void enableDragHide(boolean enableDragHide) {
        this.enableDragHide = enableDragHide;
    }

    public boolean isEnableDragHide() {
        return enableDragHide;
    }

    public Drawable getMissDrawable(Context context) {
        if (missDrawable != null)
            return missDrawable;
        else if (missPlaceHolder != 0)
            return context.getResources().getDrawable(missPlaceHolder);
        else
            return context.getResources().getDrawable(R.drawable.ic_empty_photo);
    }

    public void setMissDrawable(Drawable missDrawable) {
        this.missDrawable = missDrawable;
    }

    public Drawable getErrorDrawable(Context context) {
        if (errorDrawable != null)
            return errorDrawable;
        else if (errorPlaceHolder != 0)
            return context.getResources().getDrawable(errorPlaceHolder);
        else
            return context.getResources().getDrawable(R.drawable.ic_empty_photo);
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
    }

    List<ImageView> getOriginImageList() {
        return originImageList == null ? new ArrayList<ImageView>() : originImageList;
    }

    public void setOriginImageList(List<ImageView> originImageList) {
        this.originImageList = originImageList;
    }

    public List<String> getSourceImageList() {
        if (sourceImageList == null || sourceImageList.isEmpty()) {
            sourceImageList = new ArrayList<>();
            for (Uri uri : sourceUriList) {
                sourceImageList.add(uri.toString());
            }
        }
        return sourceImageList;
    }

    public void setSourceImageList(List<String> sourceImageList) {
        this.sourceImageList = sourceImageList;
    }

    public List<Uri> getSourceUriList() {
        return sourceUriList;
    }

    public void setSourceUriList(List<Uri> sourceUriList) {
        this.sourceUriList = sourceUriList;
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
        return (sourceImageList == null || sourceImageList.isEmpty())
                && (sourceUriList == null || sourceUriList.isEmpty());
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
     * 判断当前 position 下的资源是不是视频
     *
     * @param position 为 -1 值，表示取 nowThumbnailIndex
     * @return true : 是视频资源
     */
    public boolean isVideoSource(int position) {
        return sourceImageList.get(position == -1 ? nowThumbnailIndex : position).endsWith(".mp4");
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
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

    public View getCustomView() {
        return customView;
    }

    public void setCustomView(View customView) {
        this.customView = customView;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getFooterSize() {
        return footerSize;
    }

    public void setFooterSize(int footerSize) {
        this.footerSize = footerSize;
    }

    public static class Builder {
        private int nowThumbnailIndex;
        private int offscreenPageLimit;
        private int missPlaceHolder;
        private int errorPlaceHolder;
        private int backgroundColor;
        private long duration;
        private boolean justLoadHitPage;
        private boolean enableDragClose = true;
        private boolean enableDragHide = true;
        private boolean enableDragPause = false;
        private boolean enableAnimateShake = true;
        private boolean enableScrollingWithPageChange;

        private Drawable missDrawable;
        private Drawable errorDrawable;

        private List<String> sourceImageList;
        private List<Uri> sourceUriList;
        private List<String> thumbnailImageList;
        private List<ImageView> originImageList;

        private IProgressIndicator progressIndicator;
        private IIndexIndicator indexIndicator;
        private ImageLoader imageLoader;
        private View customView;

        private @IdRes
        int imageId;
        private ImageView imageView;
        private AbsListView listView;
        private RecyclerView recyclerView;

        private int headerSize;
        private int footerSize;

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
        public Builder setJustLoadHitPage(boolean justLoadHitPage) {
            this.justLoadHitPage = justLoadHitPage;
            return this;
        }

        /**
         * 是否可以拖拽关闭
         */
        public Builder enableDragClose(boolean enableDragClose) {
            this.enableDragClose = enableDragClose;
            return this;
        }

        /**
         * 拖拽的时候是否隐藏indicator或customView
         */
        public Builder enableDragHide(boolean enableDragHide) {
            this.enableDragHide = enableDragHide;
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
         * format: java.lang.String
         */
        public Builder setSourceImageList(List<String> sourceImageList) {
            this.sourceImageList = sourceImageList;
            return this;
        }

        /**
         * 高清图的地址集合
         * format: android.net.Uri
         */
        public Builder setSourceUriList(List<Uri> sourceUriList) {
            this.sourceUriList = sourceUriList;
            return this;
        }

//        /**
//         * 缩略图地址集合
//         */
//        public Builder setThumbnailImageList(List<String> thumbnailImageList) {
//            this.thumbnailImageList = thumbnailImageList;
//            return this;
//        }

        /**
         * 过渡前原始的 ImageView 集合
         */
        public Builder setOriginImageList(List<ImageView> originImageList) {
            this.originImageList = originImageList;
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
        public Builder setOnLongClickListener(Transferee.OnTransfereeLongClickListener listener) {
            this.longClickListener = listener;
            return this;
        }

        /**
         * 在 transferee 视图放置用户自定义的视图
         */
        public Builder setCustomView(View customView) {
            this.customView = customView;
            return this;
        }

        /**
         * 绑定 ListView
         *
         * @param imageId item layout 中的 ImageView Resource ID
         */
        public TransferConfig bindListView(AbsListView listView, int imageId) {
            this.listView = listView;
            this.imageId = imageId;
            return create();
        }

        /**
         * 绑定 ListView, 并指定 header 和 footer 的数量
         *
         * @param imageId item layout 中的 ImageView Resource ID
         */
        public TransferConfig bindListView(AbsListView listView, int headerSize, int footerSize, int imageId) {
            this.listView = listView;
            this.headerSize = headerSize;
            this.footerSize = footerSize;
            this.imageId = imageId;
            return create();
        }

        /**
         * 绑定 RecyclerView
         *
         * @param imageId item layout 中的 ImageView Resource ID
         */
        public TransferConfig bindRecyclerView(RecyclerView recyclerView, int imageId) {
            this.recyclerView = recyclerView;
            this.imageId = imageId;
            return create();
        }

        /**
         * 绑定 RecyclerView, 并指定 header 和 footer 的数量
         *
         * @param imageId item layout 中的 ImageView Resource ID
         */
        public TransferConfig bindRecyclerView(RecyclerView recyclerView, int headerSize, int footerSize, int imageId) {
            this.recyclerView = recyclerView;
            this.headerSize = headerSize;
            this.footerSize = footerSize;
            this.imageId = imageId;
            return create();
        }

        /**
         * 绑定单个 ImageView, 指定一个 sourceImageList 作为显示的图片源数据
         */
        public TransferConfig bindImageView(ImageView imageView, List<String> sourceImageList) {
            this.imageView = imageView;
            this.sourceImageList = sourceImageList;
            return create();
        }

        /**
         * 绑定单个 ImageView, 并指定图片的 url
         */
        public TransferConfig bindImageView(ImageView imageView, String sourceUrl) {
            this.imageView = imageView;
            this.sourceImageList = new ArrayList<>();
            sourceImageList.add(sourceUrl);
            return create();
        }

        public TransferConfig create() {

            TransferConfig config = new TransferConfig();

            config.setNowThumbnailIndex(nowThumbnailIndex);
            config.setOffscreenPageLimit(offscreenPageLimit);
            config.setMissPlaceHolder(missPlaceHolder);
            config.setErrorPlaceHolder(errorPlaceHolder);
            config.setBackgroundColor(backgroundColor);
            config.setDuration(duration);
            config.setJustLoadHitPage(justLoadHitPage);
            config.enableDragClose(enableDragClose);
            config.enableDragHide(enableDragHide);

            config.setMissDrawable(missDrawable);
            config.setErrorDrawable(errorDrawable);


            config.setSourceImageList(sourceImageList);
            config.setSourceUriList(sourceUriList);
            config.setOriginImageList(originImageList);
//            config.setThumbnailImageList(thumbnailImageList);

            config.setProgressIndicator(progressIndicator);
            config.setIndexIndicator(indexIndicator);
            config.setImageLoader(imageLoader);
            config.setCustomView(customView);

            config.setImageId(imageId);
            config.setImageView(imageView);
            config.setListView(listView);
            config.setRecyclerView(recyclerView);
            config.setHeaderSize(headerSize);
            config.setFooterSize(footerSize);

            config.setLongClickListener(longClickListener);

            return config;
        }
    }
}
