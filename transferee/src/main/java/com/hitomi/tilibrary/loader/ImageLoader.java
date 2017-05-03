package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.widget.ImageView;

/**
 * Created by hitomi on 2017/1/20.
 */

public interface ImageLoader {
    /**
     * 状态码，取消加载高清图
     */
    int STATUS_DISPLAY_CANCEL = -1;
    /**
     * 状态码，加载高清图失败
     */
    int STATUS_DISPLAY_FAILED = 0;
    /**
     * 状态码，加载高清图成功
     */
    int STATUS_DISPLAY_SUCCESS = 1;

    /**
     * 加载并显示原高清图
     *
     * @param srcUrl         高清图图片地址
     * @param imageView      用于图片加载成功后显示的 ImageView
     * @param placeholder    加载完成之前显示的占位图
     * @param sourceCallback 图片加载过程的回调
     */
    void showSourceImage(String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback);

    /**
     * 异步加载缩略图
     *
     * @param thumbUrl 缩略图图片地址
     * @param callback 缩略图片加载完成的回调
     */
    void loadThumbnailAsync(String thumbUrl, ImageView imageView, final ThumbnailCallback callback);

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
