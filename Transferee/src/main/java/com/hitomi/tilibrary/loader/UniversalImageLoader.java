package com.hitomi.tilibrary.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

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
 * Created by hitomi on 2017/5/3.
 * <p>
 * email: 196425254@qq.com
 */
public class UniversalImageLoader implements com.hitomi.tilibrary.loader.ImageLoader {
    private Context context;
    private DisplayImageOptions normalImageOptions;
    private Map<String, SourceCallback> callbackMap;

    private UniversalImageLoader(Context context) {
        this.context = context;
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

        normalImageOptions = new DisplayImageOptions
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
    public void showImage(final String imageUrl, final ImageView imageView, final Drawable placeholder, final SourceCallback sourceCallback) {
        callbackMap.put(imageUrl, sourceCallback);
        DisplayImageOptions options = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(placeholder)
                .showImageOnFail(placeholder)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.NONE)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoader.getInstance().displayImage(imageUrl, imageView, options, new ImageLoadingListener() {
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
                    callback.onDelivered(STATUS_DISPLAY_FAILED);
                    callbackMap.remove(imageUrl);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onFinish();
                    callback.onDelivered(STATUS_DISPLAY_SUCCESS);
                    callbackMap.remove(imageUrl);
                }

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_CANCEL);
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
    public void loadImageAsync(String imageUrl, final ThumbnailCallback callback) {
        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                callback.onFinish(null);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                callback.onFinish(new BitmapDrawable(context.getResources(), loadedImage));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                callback.onFinish(null);
            }
        });
    }

    @Override
    public Bitmap loadImageSync(String imageUrl) {
        return ImageLoader.getInstance().loadImageSync(imageUrl, normalImageOptions);
    }

    @Override
    public boolean isLoaded(String url) {
        File cache = ImageLoader.getInstance().getDiskCache().get(url);
        return cache != null && cache.exists();
    }

    @Override
    public void clearCache() {
        ImageLoader.getInstance().getMemoryCache().clear();
        ImageLoader.getInstance().getDiskCache().clear();
    }

    @Override
    public File getCache(String url) {
        return ImageLoader.getInstance().getDiskCache().get(url);
    }
}
