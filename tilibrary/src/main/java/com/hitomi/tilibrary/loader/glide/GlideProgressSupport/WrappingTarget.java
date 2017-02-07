package com.hitomi.tilibrary.loader.glide.GlideProgressSupport;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

public class WrappingTarget<Z> implements Target<Z> {

    protected final
    @NonNull
    Target<? super Z> target;

    public WrappingTarget(@NonNull Target<? super Z> target) {
        this.target = target;
    }

    public
    @NonNull
    Target<? super Z> getWrappedTarget() {
        return target;
    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        target.getSize(cb);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        target.onLoadStarted(placeholder);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        target.onLoadFailed(e, errorDrawable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
        target.onResourceReady(resource, (GlideAnimation) glideAnimation);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        target.onLoadCleared(placeholder);
    }

    @Override
    public Request getRequest() {
        return target.getRequest();
    }

    @Override
    public void setRequest(Request request) {
        target.setRequest(request);
    }

    @Override
    public void onStart() {
        target.onStart();
    }

    @Override
    public void onStop() {
        target.onStop();
    }

    @Override
    public void onDestroy() {
        target.onDestroy();
    }
}
