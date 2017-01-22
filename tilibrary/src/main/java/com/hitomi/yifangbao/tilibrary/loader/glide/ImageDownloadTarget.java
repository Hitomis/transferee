package com.hitomi.yifangbao.tilibrary.loader.glide;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;



public abstract class ImageDownloadTarget extends SimpleTarget<File> implements GlideProgressSupport.ProgressListener {
    private final String mUrl;

    protected ImageDownloadTarget(String url) {
        mUrl = url;
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        super.onLoadCleared(placeholder);
        GlideProgressSupport.forget(mUrl);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
        GlideProgressSupport.expect(mUrl, this);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable);
        GlideProgressSupport.forget(mUrl);
    }
}
