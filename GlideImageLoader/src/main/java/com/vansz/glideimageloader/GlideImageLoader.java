package com.vansz.glideimageloader;

import android.content.Context;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitomi.tilibrary.loader.ImageLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vans Z on 2020-02-28.
 * 暂不支持百分比进度指示器
 */
public class GlideImageLoader implements ImageLoader {
    private Context context;
    private Map<String, SourceCallback> callbackMap;

    private static final String CACHE_DIR = "TransGlide";

    private GlideImageLoader(Context context) {
        this.context = context;
        this.callbackMap = new HashMap<>();
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    @Override
    public void loadSource(final String imageUrl, final SourceCallback callback) {
        callbackMap.put(imageUrl, callback);
        if (callback != null) callback.onStart();
        Glide.with(context).download(imageUrl).listener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null)
                    callback.onDelivered(STATUS_DISPLAY_FAILED, null);
                callbackMap.remove(imageUrl);
                return false;
            }

            @Override
            public boolean onResourceReady(final File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                if (callback != null)
                    callback.onDelivered(STATUS_DISPLAY_SUCCESS, resource);
                callbackMap.remove(imageUrl);
                return false;
            }
        }).preload();
    }

    @Override
    public File getCache(String url) {
        File cacheFile = new File(getCacheDir(), getCacheFileName(url));
        return cacheFile.exists() ? cacheFile : null;
    }

    @Override
    public void clearCache() {
        Glide.get(context).clearMemory();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();
                FileUtils.delete(getCacheDir());
            }
        }).start();
    }

    @Override
    public File getCacheDir() {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) cacheDir.mkdirs();
        return cacheDir;
    }

    @Override
    public String getCacheFileName(String url) {
        int paramsStartIndex = url.indexOf("?");
        if (paramsStartIndex!=-1){
            url = url.substring(0,paramsStartIndex);
        }
        String[] nameArray = url.split("/");
        return nameArray[nameArray.length - 1];
    }

}
