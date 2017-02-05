package com.hitomi.yifangbao.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.widget.ImageView;

/**
 * Created by hitomi on 2017/1/20.
 */

public interface ImageLoader {

    void loadImage(String url, ImageView imageView, Drawable placeholder, final ImageLoader.Callback callback);

    void cancel();

    interface Callback {
        @UiThread
        void onStart();

        @UiThread
        void onProgress(int progress);

        @UiThread
        void onFinish();
    }
}
