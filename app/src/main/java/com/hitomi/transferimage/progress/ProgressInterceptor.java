package com.hitomi.transferimage.progress;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by chenpengfei on 2016/11/9.
 */
public class ProgressInterceptor implements Interceptor {

    private ProgressListener progressListener;

    public ProgressInterceptor(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
    }

}
