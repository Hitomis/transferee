package com.hitomi.transferimage.progress;

import android.os.Handler;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

public class ProgressModelLoader implements StreamModelLoader<String> {

    private Handler handler;

    public ProgressModelLoader(Handler handler) {
        this.handler = handler;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(String model, int width, int height) {
        return new ProgressDataFetcher(model, handler);
    }

}
