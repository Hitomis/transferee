package com.hitomi.tilibrary.loader;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.io.File;

/**
 * 图片加载器接口，实现 ImageLoader 可扩展自己的图片加载器
 * Created by Vans Z on 2017/1/20.
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
     * 加载原图
     *
     * @param imageUrl       图片地址
     * @param callback 源图加载的回调
     */
    void loadSource(String imageUrl, @Nullable final SourceCallback callback);

    /**
     * 加载缩略图
     *
     * @param imageUrl 图片地址
     * @param callback 缩略片加载的回调
     */
    void loadThumb(String imageUrl, final ThumbnailCallback callback);

    /**
     * 获取 url 关联的图片缓存
     */
    File getCache(String url);

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
        void onDelivered(int status, @Nullable File source);
    }

    interface ThumbnailCallback {
        @UiThread
        void onFinish(Bitmap bitmap);
    }
}
