package com.hitomi.yifangbao.tilibrary.loader.glide;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitomi.yifangbao.tilibrary.TransferWindow;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;

import java.io.File;

import okhttp3.OkHttpClient;



public final class GlideImageLoader extends ImageLoader {
    private final RequestManager mRequestManager;
    private Context context;

    private GlideImageLoader(Context context, OkHttpClient okHttpClient) {
        this.context = context;
        GlideProgressSupport.init(Glide.get(context), okHttpClient);
        mRequestManager = Glide.with(context);
    }

    public static GlideImageLoader with(Context context) {
        return with(context, null);
    }

    public static GlideImageLoader with(Context context, OkHttpClient okHttpClient) {
        return new GlideImageLoader(context, okHttpClient);
    }

    @Override
    public void downloadImage(final Uri uri, final int postion, final Callback callback) {
//        setCallback(callback);
        mRequestManager
                .load(uri)
                .downloadOnly(new ImageDownloadTarget(uri.toString()) {
                    @Override
                    public void onResourceReady(File image, GlideAnimation<? super File> glideAnimation) {
                        // we don't need delete this image file, so it behaves live cache hit
                        callback.onCacheHit(postion, image);
                    }

                    @Override
                    public void onDownloadStart() {
                        callback.onStart(postion);
                    }

                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(postion, progress);
                    }

                    @Override
                    public void onDownloadFinish() {
                        callback.onFinish(postion);
                    }
                });
    }

    @Override
    public void loadImage(File image, ImageView imageView) {
        Glide.with(context)
                .load(image)
                .crossFade(200)
                .into(imageView);
    }

    @Override
    public void cancel() {
        mRequestManager.onDestroy();
    }

    @Override
    public View showThumbnail(TransferWindow parent, Uri thumbnail, int scaleType) {
        ImageView thumbnailView = new ImageView(parent.getContext());
//        switch (scaleType) {
//            case BigImageView.INIT_SCALE_TYPE_CENTER_CROP:
//                thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                break;
//            case BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE:
//                thumbnailView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            default:
//                break;
//        }
        mRequestManager
                .load(thumbnail)
                .into(thumbnailView);
        return thumbnailView;
    }

    @Override
    public void prefetch(Uri uri) {
        mRequestManager
                .load(uri)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource,
                            GlideAnimation<? super File> glideAnimation) {
                        // not interested in result
                    }
                });
    }
}
