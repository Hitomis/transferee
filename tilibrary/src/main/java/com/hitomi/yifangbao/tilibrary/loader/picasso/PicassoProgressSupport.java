package com.hitomi.yifangbao.tilibrary.loader.picasso;

import com.hitomi.yifangbao.tilibrary.loader.glide.GlideProgressSupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by hitomi on 2017/1/22.
 */

public class PicassoProgressSupport {

    public static OkHttpClient.Builder init(OkHttpClient okHttpClient) {
        OkHttpClient.Builder builder;
        if (okHttpClient != null) {
            builder = okHttpClient.newBuilder();
        } else {
            builder = new OkHttpClient.Builder();
        }
        builder.addNetworkInterceptor(createInterceptor(new PicassoProgressSupport.DispatchingProgressListener()));
        return builder;
    }

    private static Interceptor createInterceptor(final PicassoProgressSupport.ResponseProgressListener listener) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                return response.newBuilder()
                        .body(new PicassoProgressSupport.OkHttpProgressResponseBody(request.url(), response.body(),
                                listener))
                        .build();
            }
        };
    }


    public interface ProgressListener {
        void onDownloadStart();

        void onProgress(int progress);

        void onDownloadFinish();
    }

    private interface ResponseProgressListener {
        void update(HttpUrl url, long bytesRead, long contentLength);
    }

    private static class DispatchingProgressListener implements PicassoProgressSupport.ResponseProgressListener {
        private static final Map<String, GlideProgressSupport.ProgressListener> LISTENERS = new HashMap<>();
        private static final Map<String, Integer> PROGRESSES = new HashMap<>();

        static void forget(String url) {
            LISTENERS.remove(url);
            PROGRESSES.remove(url);
        }

        static void expect(String url, GlideProgressSupport.ProgressListener listener) {
            LISTENERS.put(url, listener);
        }

        @Override
        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
            String key = url.toString();
            final GlideProgressSupport.ProgressListener listener = LISTENERS.get(key);
            if (listener == null) {
                return;
            }

            Integer lastProgress = PROGRESSES.get(key);
            if (lastProgress == null) {
                // ensure `onStart` is called before `onProgress` and `onFinish`
                listener.onDownloadStart();
            }
            if (contentLength <= bytesRead) {
                listener.onDownloadFinish();
                forget(key);
                return;
            }
            int progress = (int) ((float) bytesRead / contentLength * 100);
            if (lastProgress == null || progress != lastProgress) {
                PROGRESSES.put(key, progress);
                listener.onProgress(progress);
            }
        }
    }

    private static class OkHttpProgressResponseBody extends ResponseBody {
        private final HttpUrl mUrl;
        private final ResponseBody mResponseBody;
        private final PicassoProgressSupport.ResponseProgressListener mProgressListener;
        private BufferedSource mBufferedSource;

        OkHttpProgressResponseBody(HttpUrl url, ResponseBody responseBody,
                                   PicassoProgressSupport.ResponseProgressListener progressListener) {
            this.mUrl = url;
            this.mResponseBody = responseBody;
            this.mProgressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return mResponseBody.contentType();
        }

        @Override
        public long contentLength() {
            return mResponseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (mBufferedSource == null) {
                mBufferedSource = Okio.buffer(source(mResponseBody.source()));
            }
            return mBufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                private long mTotalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    long fullLength = mResponseBody.contentLength();
                    if (bytesRead == -1) { // this source is exhausted
                        mTotalBytesRead = fullLength;
                    } else {
                        mTotalBytesRead += bytesRead;
                    }
                    mProgressListener.update(mUrl, mTotalBytesRead, fullLength);
                    return bytesRead;
                }
            };
        }
    }

}
