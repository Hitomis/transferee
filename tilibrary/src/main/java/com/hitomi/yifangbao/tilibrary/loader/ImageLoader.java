package com.hitomi.yifangbao.tilibrary.loader;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;
import android.view.View;

import com.hitomi.yifangbao.tilibrary.TransferWindow;

import java.io.File;

/**
 * Created by hitomi on 2017/1/20.
 */

public abstract class ImageLoader {

    public static final int STATUS_START = 1001;
    public static final int STATUS_PROGRESS = 1002;
    public static final int STATUS_FINISH = 1003;
    public static final int STATUS_CACHE_MISS = 1004;

    private ImageLoader.Callback callback;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (callback == null) return ;
            switch (msg.what) {
                case STATUS_START:
                    callback.onStart();
                    break;
                case STATUS_PROGRESS:
                    callback.onProgress((int) msg.obj);
                    break;
                case STATUS_FINISH:
                    callback.onFinish();
                    break;
                case STATUS_CACHE_MISS:
                    callback.onCacheMiss((File) msg.obj);
                    break;
            }
        }
    };

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void postMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public abstract void loadImage(Uri uri, Callback callback);

    public abstract View showThumbnail(TransferWindow parent, Uri thumbnail, int scaleType);

    public abstract void prefetch(Uri uri);

    public interface Callback {
        @UiThread
        void onCacheHit(File image);

        @UiThread
        void onCacheMiss(File image);

        @UiThread
        void onStart();

        @UiThread
        void onProgress(int progress);

        @UiThread
        void onFinish();
    }
}
