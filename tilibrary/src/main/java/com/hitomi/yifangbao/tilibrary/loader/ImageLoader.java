package com.hitomi.yifangbao.tilibrary.loader;

import android.net.Uri;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.TransferWindow;

import java.io.File;

/**
 * Created by hitomi on 2017/1/20.
 */

public interface ImageLoader {

    void downloadImage(Uri uri, int postion, Callback callback);

    void loadImage(File image, ImageView imageView);

    void cancel();

    View showThumbnail(TransferWindow parent, Uri thumbnail, int scaleType);

    void prefetch(Uri uri);

    interface Callback {
        @UiThread
        void onCacheHit(int position, File image);

        @UiThread
        void onCacheMiss(int position, File image);

        @UiThread
        void onStart(int position);

        @UiThread
        void onProgress(int position, int progress);

        @UiThread
        void onFinish(int position);
    }
}
