package com.hitomi.yifangbao.tilibrary.loader.picasso;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.TransferWindow;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Created by hitomi on 2017/1/22.
 */

public class PicassoImageLoader extends ImageLoader {

    private final Picasso.Builder builder;
    private OkHttpClient client;

    private PicassoImageLoader(Context context, OkHttpClient okHttpClient) {
        client = PicassoProgressSupport.init(okHttpClient).build();
        builder = new Picasso.Builder(context);
    }

    public static PicassoImageLoader with(Context context) {
        return with(context, null);
    }

    public static PicassoImageLoader with(Context context, OkHttpClient okHttpClient) {
        return new PicassoImageLoader(context, okHttpClient);
    }

    @Override
    public void downloadImage(Uri uri, int position, Callback callback) {
        builder.downloader(new PicassoImageDownloader(client) {
            @Override
            public void onDownloadStart() {
                System.out.println(1111);
            }

            @Override
            public void onProgress(int progress) {
                System.out.println(2222);
            }

            @Override
            public void onDownloadFinish() {
                System.out.println(3333);
            }
        }).build().load(uri);
    }

    @Override
    public void loadImage(File image, ImageView imageView) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public View showThumbnail(TransferWindow parent, Uri thumbnail, int scaleType) {
        return null;
    }

    @Override
    public void prefetch(Uri uri) {

    }
}
