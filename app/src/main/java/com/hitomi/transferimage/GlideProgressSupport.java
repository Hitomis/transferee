package com.hitomi.transferimage;

import android.os.Handler;
import android.os.Message;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by hitomi on 2017/1/25.
 */

public class GlideProgressSupport {

    public static DataModelLoader init(Handler handler) {
        return new DataModelLoader(handler);
    }

    public interface ResponseProgressListener {
        void progress(long bytesRead, long contentLength, boolean done);
    }

    public static class DataModelLoader implements StreamModelLoader<String> {
        private Handler handler;

        public DataModelLoader(Handler handler) {
            this.handler = handler;
        }

        @Override
        public DataFetcher<InputStream> getResourceFetcher(String model, int width, int height) {
            return new ProgressDataFetcher(model, handler);
        }
    }

    private static class ProgressDataFetcher implements DataFetcher<InputStream> {

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
                handler.sendEmptyMessage(GlideImageLoader.MSG_START);
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

        private ResponseProgressListener getProgressListener() {
            ResponseProgressListener progressListener = new ResponseProgressListener() {

                @Override
                public void progress(long bytesRead, long contentLength, boolean done) {
                    Message message = handler.obtainMessage();
                    if (handler != null && !done) {
                        message.what = GlideImageLoader.MSG_PROGRESS;
                        message.arg1 = (int) bytesRead;
                        message.arg2 = (int) contentLength;
                    }
                    if (done) {
                        message.what = GlideImageLoader.MSG_FINISH;
                    }
                    handler.sendMessage(message);
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

    private static class ProgressInterceptor implements Interceptor {

        private ResponseProgressListener progressListener;

        public ProgressInterceptor(ResponseProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
        }

    }

    private static class ProgressResponseBody extends ResponseBody {

        private ResponseBody responseBody;
        private ResponseProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ResponseProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return responseBody.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                try {
                    bufferedSource = Okio.buffer(source(responseBody.source()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {

                long totalBytesRead = 0;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (progressListener != null)
                        progressListener.progress(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

}
