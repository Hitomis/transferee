package com.vansz.picassoimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.blankj.utilcode.util.FileUtils;
import com.hitomi.tilibrary.loader.ImageLoader;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vans Z on 2020-02-28.
 */
public class PicassoImageLoader implements ImageLoader {
    private Picasso picasso;
    private Context context;
    private Map<String, SourceCallback> callbackMap;

    private static final String CACHE_DIR = "TransPicasso";

    private PicassoImageLoader(Context context) {
        this.context = context;
        this.callbackMap = new HashMap<>();
        initImageLoader();

    }

    private void initImageLoader() {
        Picasso.Builder builder = new Picasso.Builder(context);
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        final long diskCacheSize = 512 * 1024 * 1024;
        picasso = builder
                .downloader(new OkHttp3Downloader(cacheDir, diskCacheSize))
                .defaultBitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public static PicassoImageLoader with(Context context) {
        return new PicassoImageLoader(context);
    }

    @Override
    public void showImage(final String imageUrl, final ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        callbackMap.put(imageUrl, sourceCallback);
        picasso.load(imageUrl).placeholder(placeholder).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (!imageUrl.endsWith(".gif")) // gif 图片需要 transferee 内部渲染，所以这里作显示
                    imageView.setImageBitmap(bitmap);
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_SUCCESS, null);
                    callbackMap.remove(imageUrl);
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_FAILED, null);
                    callbackMap.remove(imageUrl);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null)
                    callback.onStart();
            }
        });

    }

    @Override
    public void loadImageAsync(final String imageUrl, final ThumbnailCallback callback) {
        picasso.load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (callback != null)
                    callback.onFinish(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                if (callback != null)
                    callback.onFinish(null);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public Bitmap loadImageSync(String imageUrl) {
        return BitmapFactory.decodeFile(getCache(imageUrl).getAbsolutePath());
    }

    @Override
    public File getCache(String url) {
        // 通过分析 OkHttp3Downloader 源码可知缓存文件名是图片 url 经过 md5 加密后的小写字符串，并拼接 ".1"
        // .0 表示缓存文件的网络请求 header 描述; .1 是缓存文件本身
        String key = EncryptUtils.encryptMD5ToString(url).toLowerCase() + ".1";
        File cacheFile = new File(getCacheDir(), key);
        return cacheFile.exists() ? cacheFile : null;
    }

    @Override
    public void clearCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.delete(getCacheDir());
            }
        }).start();
    }

    private File getCacheDir() {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) cacheDir.mkdirs();
        return cacheDir;
    }
}
