package com.hitomi.tilibrary.loader.glide.GlideProgressSupport;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;

public abstract class ProgressTarget<T, Z> extends WrappingTarget<Z> implements OkHttpProgressGlideModule.UIProgressListener {
    private T model;
    private boolean ignoreProgress = true;
    private boolean first = true;

    public ProgressTarget(Target<Z> target) {
        this(null, target);
    }

    public ProgressTarget(T model, Target<Z> target) {
        super(target);
        this.model = model;
    }

    public final T getModel() {
        return model;
    }

    public final void setModel(T model) {
        Glide.clear(this); // indirectly calls cleanup
        this.model = model;
    }

    /**
     * Convert a model into an Url string that is used to match up the OkHttp requests. For explicit
     * {@link com.bumptech.glide.load.model.GlideUrl GlideUrl} loads this needs to return
     * {@link com.bumptech.glide.load.model.GlideUrl#toStringUrl toStringUrl}. For custom models do the same as your
     * {@link com.bumptech.glide.load.model.stream.BaseGlideUrlLoader BaseGlideUrlLoader} does.
     *
     * @param model return the representation of the given model, DO NOT use {@link #getModel()} inside this method.
     * @return a stable Url representation of the model, otherwise the progress reporting won't work
     */
    protected String toUrlString(T model) {
        return String.valueOf(model);
    }

    @Override
    public float getGranularityPercentage() {
        return 1.0f;
    }

    @Override
    public void onProgress(long bytesRead, long expectedLength) {
        if (ignoreProgress) {
            return;
        }
        if (first) {
            onStartDownload();
            first = false;
        } else if (bytesRead == expectedLength) {
            onDownloaded();
        } else {
            onDownloading(bytesRead, expectedLength);
        }
    }

    /**
     * Called when the Glide load has started.
     * At this time it is not known if the Glide will even go and use the network to fetch the image.
     */
    protected abstract void onStartDownload();

    /**
     * Called when there's any progress on the download; not called when loading from cache.
     * At this time we know how many bytes have been transferred through the wire.
     */
    protected abstract void onDownloading(long bytesRead, long expectedLength);

    /**
     * Called when the bytes downloaded reach the length reported by the server; not called when loading from cache.
     * At this time it is fairly certain, that Glide either finished reading the stream.
     * This means that the image was either already decoded or saved the network stream to cache.
     * In the latter case there's more work to do: decode the image from cache and transform.
     * These cannot be listened to for progress so it's unsure how fast they'll be, best to show indeterminate progress.
     */
    protected abstract void onDownloaded();

    /**
     * Called when the Glide load has finished either by successfully loading the image or failing to load or cancelled.
     * In any case the best is to hide/reset any progress displays.
     */
    protected abstract void onDelivered();

    private void start() {
        OkHttpProgressGlideModule.expect(toUrlString(model), this);
        ignoreProgress = false;
    }

    private void cleanup() {
        ignoreProgress = true;
        T model = this.model; // save in case it gets modified
        onDelivered();
        OkHttpProgressGlideModule.forget(toUrlString(model));
        this.model = null;
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
        start();
    }

    @Override
    public void onResourceReady(Z resource, GlideAnimation<? super Z> animation) {
        cleanup();
        super.onResourceReady(resource, animation);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        cleanup();
        super.onLoadFailed(e, errorDrawable);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        cleanup();
        super.onLoadCleared(placeholder);
    }
}