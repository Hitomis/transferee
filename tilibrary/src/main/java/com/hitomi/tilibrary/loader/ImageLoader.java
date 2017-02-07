package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.widget.ImageView;

/**
 * Created by hitomi on 2017/1/20.
 */

public interface ImageLoader {
    /**
     * 开始加载图片
     * @param url 图片地址
     * @param imageView 用于图片加载成功后显示的 ImageView
     * @param placeholder 加载完成之前显示的占位图
     * @param callback 图片加载过程的回调
     */
    void loadImage(String url, ImageView imageView, Drawable placeholder, final ImageLoader.Callback callback);

    /**
     * 后台预加载图片资源
     * @param url
     */
    void preFetch(String url);

    /**
     * 取消图片加载
     */
    void cancel();

    interface Callback {
        @UiThread
        void onStart();

        @UiThread
        void onProgress(int progress);

        @UiThread
        void onFinish();
    }
}
