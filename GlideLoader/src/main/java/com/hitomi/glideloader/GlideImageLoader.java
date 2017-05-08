package com.hitomi.glideloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.hitomi.glideloader.support.ProgressTarget;
import com.hitomi.tilibrary.loader.ImageLoader;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;


/**
 * 使用 <a href="https://github.com/bumptech/glide">Glide</a>
 * 作为 Transferee 的图片加载器
 * <p>
 * Created by hitomi on 2017/1/25.
 */
public class GlideImageLoader implements ImageLoader {
    private static final String SP_FILE = "transferee";
    private static final String SP_LOAD_SET = "load_set";

    private Context context;
    private SharedPreferences loadSharedPref;

    private GlideImageLoader(Context context) {
        this.context = context;
        loadSharedPref = context.getSharedPreferences(
                SP_FILE, Context.MODE_PRIVATE);
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    @Override
    public void showSourceImage(final String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        ProgressTarget<String, GlideDrawable> progressTarget =
                new ProgressTarget<String, GlideDrawable>(srcUrl, new GlideDrawableImageViewTarget(imageView)) {

                    @Override
                    protected void onStartDownload() {
                        sourceCallback.onStart();
                    }

                    @Override
                    protected void onDownloading(long bytesRead, long expectedLength) {
                        sourceCallback.onProgress((int) (bytesRead * 100 / expectedLength));
                    }

                    @Override
                    protected void onDownloaded() {
                        sourceCallback.onFinish();
                    }

                    @Override
                    protected void onDelivered(int status) {
                        if (status == STATUS_DISPLAY_SUCCESS)
                            cacheLoadedImageUrl(srcUrl);
                        sourceCallback.onDelivered(status);
                    }
                };

        Glide.with(context)
                .load(srcUrl)
                .dontAnimate()
                .placeholder(placeholder)
                .into(progressTarget);
    }

    @Override
    public void loadThumbnailAsync(String thumbUrl, ImageView imageView, final ThumbnailCallback callback) {
        ProgressTarget<String, GlideDrawable> progressTarget =
                new ProgressTarget<String, GlideDrawable>(thumbUrl, new GlideDrawableImageViewTarget(imageView)) {

                    @Override
                    protected void onStartDownload() {
                    }

                    @Override
                    protected void onDownloading(long bytesRead, long expectedLength) {
                    }

                    @Override
                    protected void onDownloaded() {
                    }

                    @Override
                    protected void onDelivered(int status) {
                    }

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        callback.onFinish(resource);
                    }
                };

        Glide.with(context)
                .load(thumbUrl)
                .dontAnimate()
                .into(progressTarget);
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
