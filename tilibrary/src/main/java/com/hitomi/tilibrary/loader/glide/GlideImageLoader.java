package com.hitomi.tilibrary.loader.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.loader.glide.GlideProgressSupport.ProgressTarget;

import java.io.File;


/**
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
    public void displaySourceImage(String srcUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        ProgressTarget<String, Bitmap> progressTarget = new ProgressTarget<String, Bitmap>(srcUrl, new BitmapImageViewTarget(imageView)) {

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
            protected void onDelivered() {
            }
        };

        Glide.with(context)
                .load(srcUrl)
                .asBitmap()
                .dontAnimate()
                .placeholder(placeholder)
                .into(progressTarget);
    }

    @Override
    public void displayThumbnailImageAsync(String thumbUrl, final ThumbnailCallback callback) {
        Glide.with(context)
                .load(thumbUrl)
                .dontAnimate()
                .override(100, 100)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        callback.onFinish(resource);
                    }
                });
    }

    @Override
    public void preFetch(String url) {
        Glide.with(context)
                .load(url)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        // Don't do anything
                    }
                });
    }

}
