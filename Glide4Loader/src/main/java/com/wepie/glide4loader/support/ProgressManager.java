package com.wepie.glide4loader.support;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.GlideException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sunfusheng on 2017/6/14.
 */
public class ProgressManager {

    private static List<WeakReference<OnProgressListener>> listeners = Collections.synchronizedList(new ArrayList<WeakReference<OnProgressListener>>());
    private static OkHttpClient okHttpClient;

    private ProgressManager() {
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            Request request = chain.request();
                            Response response = chain.proceed(request);
                            return response.newBuilder()
                                    .body(new ProgressResponseBody(request.url().toString(), response.body(), LISTENER))
                                    .build();
                        }
                    })
                    .build();
        }
        return okHttpClient;
    }

    private static final OnProgressListener LISTENER = new OnProgressListener() {
        @Override
        public void onProgress(String imageUrl, long bytesRead, long totalBytes, boolean isDone, GlideException exception) {
            if (listeners == null || listeners.size() == 0) return;

            for (int i = 0; i < listeners.size(); i++) {
                WeakReference<OnProgressListener> listener = listeners.get(i);
                OnProgressListener progressListener = listener.get();
                if (progressListener == null) {
                    listeners.remove(i);
                } else {
                    progressListener.onProgress(imageUrl, bytesRead, totalBytes, isDone, exception);
                }
            }
        }
    };

    public static void addProgressListener(OnProgressListener progressListener) {
        if (progressListener == null) return;

        if (findProgressListener(progressListener) == null) {
            listeners.add(new WeakReference<>(progressListener));
        }
    }

    public static void removeProgressListener(OnProgressListener progressListener) {
        if (progressListener == null) return;

        WeakReference<OnProgressListener> listener = findProgressListener(progressListener);
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private static WeakReference<OnProgressListener> findProgressListener(OnProgressListener listener) {
        if (listener == null) return null;
        if (listeners == null || listeners.size() == 0) return null;

        for (int i = 0; i < listeners.size(); i++) {
            WeakReference<OnProgressListener> progressListener = listeners.get(i);
            if (progressListener.get() == listener) {
                return progressListener;
            }
        }
        return null;
    }
}
