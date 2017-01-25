package com.hitomi.transferimage;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;

/**
 * Created by hitomi on 2017/1/25.
 */

public class GlideImageLoader {

    static final int MSG_START = 1 << 1;
    static final int MSG_PROGRESS = 1 << 2;
    static final int MSG_FINISH = 1 << 3;

    private Context context;

    private GlideImageLoader(Context context) {
        this.context = context;
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    public void loadImage(String url, ImageView imageView, int placeholder, final ImageLoader.Callback callback) {
        GlideProgressSupport.DataModelLoader modelLoader = GlideProgressSupport.init(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_START:
                        callback.onStart(0);
                        break;
                    case MSG_PROGRESS:
                        int percent = msg.arg1 * 100 / msg.arg2;
                        callback.onProgress(0, percent);
                        break;
                    case MSG_FINISH:
                        callback.onFinish(0);
                        break;
                }
            }
        });

        Glide.with(context)
                .using(modelLoader)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .placeholder(placeholder)
                .priority(Priority.IMMEDIATE)
                .into(imageView);
    }


}
