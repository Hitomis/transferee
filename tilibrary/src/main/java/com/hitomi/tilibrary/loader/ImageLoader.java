package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.widget.ImageView;

/**
 * Created by hitomi on 2017/1/20.
 */

public interface ImageLoader {
    /**
     * 加载并显示原高清图
     *
     * @param url            图片地址
     * @param imageView      用于图片加载成功后显示的 ImageView
     * @param placeholder    加载完成之前显示的占位图
     * @param sourceCallback 图片加载过程的回调
     */
    void displaySourceImage(String url, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback);

    /**
     * 加载并显示缩略图
     *
     * @param url      缩略图图片地址
     * @param callback 缩略图片加载完成的回调
     */
    void displayThumbnailImage(String url, final ThumbnailCallback callback);

    /**
     * 后台预加载图片资源
     *
     * @param url
     */
    void preFetch(String url);

    interface SourceCallback {
        @UiThread
        void onStart();

        @UiThread
        void onProgress(int progress);

        @UiThread
        void onFinish();
    }

    interface ThumbnailCallback {
        @UiThread
        void onFinish(Drawable drawable);
    }
}
