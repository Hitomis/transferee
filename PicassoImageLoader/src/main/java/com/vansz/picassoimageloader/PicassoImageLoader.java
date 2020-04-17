package com.vansz.picassoimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.hitomi.tilibrary.loader.ImageLoader;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Vans Z on 2020-02-28.
 * 暂不支持百分比进度指示器
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
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        final long diskCacheSize = 512 * 1024 * 1024;
        Picasso.Builder builder = new Picasso.Builder(context);
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
        // 因为 picasso 不支持 gif 图显示，也不支持 download 或者 asFile 操作。
        // 所以如果是 gif 图片,暂时直接使用 OkHttp3 下载之后回传给 Transferee 渲染
        if (imageUrl.endsWith(".gif")) {
            showGif(imageUrl, sourceCallback);
        } else {
            showImage(imageUrl, imageView, placeholder);
        }
    }

    private void showImage(final String imageUrl, final ImageView imageView, Drawable placeholder) {
        picasso.load(imageUrl).placeholder(placeholder).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
                SourceCallback callback = callbackMap.get(imageUrl);
                if (callback != null) {
                    callback.onDelivered(STATUS_DISPLAY_SUCCESS, getCache(imageUrl));
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

    private void showGif(final String imageUrl, SourceCallback sourceCallback) {
        if (sourceCallback != null) sourceCallback.onStart();
        File cacheGif = getCache(imageUrl);
        if (cacheGif == null) { // 没有缓存，使用 okhttp3 下载并保存到指定文件夹
            Request gifRequest = new Request.Builder()
                    .url(imageUrl)
                    .get()
                    .build();
            Call call = new OkHttpClient().newCall(gifRequest);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    SourceCallback callback = callbackMap.get(imageUrl);
                    if (callback != null) {
                        callback.onDelivered(STATUS_DISPLAY_FAILED, null);
                        callbackMap.remove(imageUrl);
                    }
                }

                @Override
                public void onResponse(final Call call, final Response response) {
                    if (response.body() != null && response.body().byteStream() != null) {
                        cacheAndDelivered(response, imageUrl);
                    }
                }
            });
        } else {
            if (sourceCallback != null)
                sourceCallback.onDelivered(STATUS_DISPLAY_SUCCESS, cacheGif);
            callbackMap.remove(imageUrl);
        }
    }

    private void cacheAndDelivered(final Response response, final String imageUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = EncryptUtils.encryptMD5ToString(imageUrl).toLowerCase() + ".1";
                final File cacheGif = new File(getCacheDir(), key);
                boolean success = FileIOUtils.writeFileFromIS(cacheGif, response.body().byteStream());
                if (!success) {
                    callbackMap.remove(imageUrl);
                    return;
                }
                // 主线程通知 transferee 渲染 gif 图片
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        SourceCallback callback = callbackMap.get(imageUrl);
                        if (callback != null) {
                            callback.onDelivered(STATUS_DISPLAY_SUCCESS, cacheGif);
                            callbackMap.remove(imageUrl);
                        }
                    }
                });
            }

        }).start();
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
