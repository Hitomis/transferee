package com.hitomi.yifangbao.tilibrary.loader.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hitomi on 2017/1/25.
 */
public class GlideImageLoader implements ImageLoader {

    static final int MSG_START = 1 << 1;
    static final int MSG_PROGRESS = 1 << 2;
    static final int MSG_FINISH = 1 << 3;

    private Context context;

    private Set<GlideProgressSupport.DataModelLoader> loaderSupportSet;

    private GlideImageLoader(Context context) {
        loaderSupportSet = new HashSet<>();
        this.context = context;
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    @Override
    public void loadImage(String url, ImageView imageView, Drawable placeholder, final Callback callback) {
        Glide.with(context)
                .using(getDataModelLoader(callback))
                .load(url)
                .dontAnimate()
                .placeholder(placeholder)
                .into(imageView);
    }

    @NonNull
    private GlideProgressSupport.DataModelLoader getDataModelLoader(final Callback callback) {
        GlideProgressSupport.DataModelLoader loaderSupport = GlideProgressSupport.init(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_START:
                        callback.onStart();
                        break;
                    case MSG_PROGRESS:
                        callback.onProgress(msg.arg1 * 100 / msg.arg2);
                        break;
                    case MSG_FINISH:
                        callback.onFinish();
                        break;
                }
            }
        });
        loaderSupportSet.add(loaderSupport);
        return loaderSupport;
    }

    @Override
    public void cancel() {
        for (GlideProgressSupport.DataModelLoader loaderSupport : loaderSupportSet) {
            loaderSupport.cancel();
        }
    }


}
