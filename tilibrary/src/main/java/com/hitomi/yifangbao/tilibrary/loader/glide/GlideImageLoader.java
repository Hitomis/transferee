/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
