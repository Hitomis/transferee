package com.wepie.glide4loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hitomi.tilibrary.loader.ImageLoader;
import com.wepie.glide4loader.support.OnProgressListener;
import com.wepie.glide4loader.support.ProgressManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;

/**
 * Created by hitomi on 2017/8/24.
 */
public class Glide4ImageLoader implements ImageLoader {

    private static final String SP_FILE = "transferee";
    private static final String SP_LOAD_SET = "load_set";

    private Context context;
    private Handler handler;
    private SharedPreferences loadSharedPref;
    private Map<String, SourceCallback> callbackMap;

    private Glide4ImageLoader(Context context) {
        this.context = context;
        handler = new Handler();
        callbackMap = new WeakHashMap<>();
        loadSharedPref = context.getSharedPreferences(
                SP_FILE, Context.MODE_PRIVATE);
    }

    public static Glide4ImageLoader with(Context context) {
        return new Glide4ImageLoader(context);
    }


    @Override
    public void showSourceImage(final String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        if (!callbackMap.containsKey(srcUrl)) callbackMap.put(srcUrl, sourceCallback);

        final OnProgressListener onProgressListener = new OnProgressListener() {
            @Override
            public void onProgress(final String imageUrl, final long bytesRead, final long totalBytes, boolean isDone, GlideException exception) {
                if (isDone)
                    callbackMap.remove(imageUrl);
                else
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SourceCallback sourceCallback1 = callbackMap.get(imageUrl);
                            if (sourceCallback1 != null)
                                sourceCallback1.onProgress((int) (bytesRead * 100 / totalBytes));
                        }
                    });
            }
        };
        ProgressManager.addProgressListener(onProgressListener);

        RequestOptions requestOptions = new RequestOptions()
                .dontAnimate()
                .placeholder(placeholder);
        Glide.with(context)
                .load(srcUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        ProgressManager.removeProgressListener(onProgressListener);
                        sourceCallback.onDelivered(STATUS_DISPLAY_FAILED);
                        callbackMap.clear();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ProgressManager.removeProgressListener(onProgressListener);
                        sourceCallback.onDelivered(STATUS_DISPLAY_SUCCESS);
                        callbackMap.clear();
                        cacheLoadedImageUrl(srcUrl);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public void loadThumbnailAsync(final String thumbUrl, ImageView imageView, final ThumbnailCallback callback) {
        Glide.with(context)
                .load(thumbUrl)
                .apply(new RequestOptions().dontAnimate())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        callback.onFinish(resource);
                        cacheLoadedImageUrl(thumbUrl);
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public boolean isLoaded(String url) {
        Set<String> loadedSet = loadSharedPref.getStringSet(SP_LOAD_SET, new HashSet<String>());
        return loadedSet.contains(url);
    }

    @Override
    public void clearCache() {
        loadSharedPref.edit()
                .remove(SP_LOAD_SET)
                .apply();

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();
                Glide.get(context).clearMemory();
            }
        });

    }

    /**
     * 使用 GlideImageLoader 时，需要缓存已加载完成的图片Url
     *
     * @param url 加载完成的图片Url
     */
    public void cacheLoadedImageUrl(final String url) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
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

}
