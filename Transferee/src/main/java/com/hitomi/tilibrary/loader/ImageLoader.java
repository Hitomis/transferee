package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.widget.ImageView;

/**
 * 图片加载器接口，实现 ImageLoader 可扩展自己的图片加载器
 * Created by hitomi on 2017/1/20.
 * <p>
 * email: 196425254@qq.com
 */
public interface ImageLoader {
    /**
     * 状态码，取消加载原图
     */
    int STATUS_DISPLAY_CANCEL = -1;
    /**
     * 状态码，加载原图失败
     */
    int STATUS_DISPLAY_FAILED = 0;
    /**
     * 状态码，加载原图成功
     */
    int STATUS_DISPLAY_SUCCESS = 1;

    /**
     * 加载并显示原图
     *
     * @param imageUrl       图片地址
     * @param imageView      用于图片加载成功后显示的 ImageView
     * @param placeholder    加载完成之前显示的占位图
     * @param sourceCallback 图片加载过程的回调
     */
    void showImage(String imageUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback);

    /**
     * 异步加载图片
     *
     * @param imageUrl 图片地址
     * @param callback 片加载完成的回调
     */
    void loadImageAsync(String imageUrl, final ThumbnailCallback callback);

    /**
     * 同步加载图片，返回 Drawable
     * @param imageUrl
     * @return
     */
    Drawable loadImageSync(String imageUrl);

    /**
     * 检查本地磁盘缓存中是否已经存在此路径所关联的图片
     *
     * @param url 图片路径
     * @return true: 已加载, false: 未加载
     */
    boolean isLoaded(String url);

    /**
     * 清除 ImageLoader 缓存
     */
    void clearCache();

    interface SourceCallback {
        @UiThread
        void onStart();

        @UiThread
        void onProgress(int progress);

        @UiThread
        void onFinish();

        @UiThread
        void onDelivered(int status);
    }

    interface ThumbnailCallback {
        @UiThread
        void onFinish(Drawable drawable);
    }
}
