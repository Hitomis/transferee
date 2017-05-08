package com.hitomi.glideloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.hitomi.glideloader.support.ProgressTarget;
import com.hitomi.tilibrary.loader.ImageLoader;


/**
 * 使用 <a href="https://github.com/bumptech/glide">Glide</a>
 * 作为 Transferee 的图片加载器
 * <p>
 * Created by hitomi on 2017/1/25.
 */
public class GlideImageLoader implements ImageLoader {
    private Context context;

    private GlideImageLoader(Context context) {
        this.context = context;
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    @Override
    public void showSourceImage(String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
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
        return false;
    }

    public void stayRequests(Context context, boolean stay) {
        if (stay) {
            Glide.with(context).pauseRequests();
        } else {
            Glide.with(context).resumeRequests();
        }
    }


}
