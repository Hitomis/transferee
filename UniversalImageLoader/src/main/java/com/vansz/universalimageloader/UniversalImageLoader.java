package com.vansz.universalimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 <a href="https://github.com/nostra13/Android-Universal-Image-Loader">
 * Android-Universal-Image-Loader</a>作为 Transferee 的图片加载器
 * <p>
 * Created by Vans Z on 2017/5/3.
 * <p>
 * email: 196425254@qq.com
 */
public class UniversalImageLoader implements com.hitomi.tilibrary.loader.ImageLoader {
    private Map<String, SourceCallback> callbackMap;

    private UniversalImageLoader(Context context) {
        this.callbackMap = new HashMap<>();
        initImageLoader(context);
    }

    public static UniversalImageLoader with(Context context) {
        return new UniversalImageLoader(context);
    }

    private void initImageLoader(Context context) {
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 3);
        MemoryCache memoryCache;
        memoryCache = new LruMemoryCache(memoryCacheSize);

        DisplayImageOptions normalImageOptions = new DisplayImageOptions
                .Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
                .Builder(context)
                .defaultDisplayImageOptions(normalImageOptions)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(memoryCache)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(3)
                .imageDownloader(new BaseImageDownloader(context, 15000, 15000))
                .build();

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }

    @Override
    public void loadSource(final String imageUrl, final SourceCallback callback) {
        callbackMap.put(imageUrl, callback);
        DisplayImageOptions options = new DisplayImageOptions
                .Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.NONE)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoader.getInstance().loadImage(imageUrl, null, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null)
                    callback.onStart();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_FAILED, null);
                    callbackMap.remove(imageUrl);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_SUCCESS, getCache(imageUri));
                    callbackMap.remove(imageUrl);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_FAILED, null);
                    callbackMap.remove(imageUrl);
                }
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null)
                    callback.onProgress(current * 100 / total);
            }
        });
    }

    @Override
    public void clearCache() {
        ImageLoader.getInstance().getMemoryCache().clear();
        ImageLoader.getInstance().getDiskCache().clear();
    }

    @Override
    public File getCache(String url) {
        File cache = ImageLoader.getInstance().getDiskCache().get(url);
        return (cache != null && cache.exists()) ? cache : null;
    }

    @Override
    public File getCacheDir() {
        return ImageLoader.getInstance().getDiskCache().getDirectory();
    }

    @Override
    public String getCacheFileName(String url) {
        String[] nameArray = url.split("/");
        return nameArray[nameArray.length - 1];
    }
}
