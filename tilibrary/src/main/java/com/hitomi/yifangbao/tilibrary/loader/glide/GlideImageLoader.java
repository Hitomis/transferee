package com.hitomi.yifangbao.tilibrary.loader.glide;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
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

    private GlideImageLoader(Context context, OkHttpClient okHttpClient) {
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
    public void loadImage(final Uri uri, final Callback callback) {
        setCallback(callback);
        mRequestManager
                .load(uri)
                .downloadOnly(new ImageDownloadTarget(uri.toString()) {
                    @Override
                    public void onResourceReady(File image, GlideAnimation<? super File> glideAnimation) {
                        // we don't need delete this image file, so it behaves live cache hit
                        callback.onCacheHit(image);
                    }

                    @Override
                    public void onDownloadStart() {
                        Message msg = Message.obtain();
                        msg.what = ImageLoader.STATUS_START;
                        postMessage(msg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        Message msg = Message.obtain();
                        msg.what = ImageLoader.STATUS_PROGRESS;
                        msg.obj = progress;
                        postMessage(msg);
                    }

                    @Override
                    public void onDownloadFinish() {
                        Message msg = Message.obtain();
                        msg.what = ImageLoader.STATUS_FINISH;
                        postMessage(msg);
                    }
                });
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
