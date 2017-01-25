package com.hitomi.transferimage.progress;

import android.os.Handler;
import android.os.Message;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chenpengfei on 2016/11/9.
 */
public class ProgressDataFetcher implements DataFetcher<InputStream> {

    private String url;
    private Handler handler;
    private Call progressCall;
    private InputStream stream;
    private boolean isCancelled;

    public ProgressDataFetcher(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new ProgressInterceptor(getProgressListener()));
        try {
            progressCall = client.newCall(request);
            Response response = progressCall.execute();
            if (isCancelled) {
                return null;
            }
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            stream = response.body().byteStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stream;
    }

    private ProgressListener getProgressListener() {
         ProgressListener progressListener = new ProgressListener() {

            @Override
            public void progress(long bytesRead, long contentLength, boolean done) {
                if (handler != null && !done) {
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    message.arg1 = (int)bytesRead;
                    message.arg2 = (int)contentLength;
                    handler.sendMessage(message);
                }
            }
        };
        return progressListener;
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
                stream = null;
            } catch (IOException e) {
                stream = null;
            }
        }
        if (progressCall != null) {
            progressCall.cancel();
        }
    }

    @Override
    public String getId() {
        return url;
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }
}
