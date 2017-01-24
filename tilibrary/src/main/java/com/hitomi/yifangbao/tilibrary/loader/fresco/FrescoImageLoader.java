package com.hitomi.yifangbao.tilibrary.loader.fresco;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.hitomi.yifangbao.tilibrary.TransferWindow;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public final class FrescoImageLoader implements ImageLoader {

    private final Context mAppContext;
    private final DefaultExecutorSupplier mExecutorSupplier;
    private List<DataSource<CloseableReference<PooledByteBuffer>>> sourceList;

    private FrescoImageLoader(Context appContext) {
        mAppContext = appContext;
        mExecutorSupplier = new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors());
        sourceList = new ArrayList<>();
    }

    public static FrescoImageLoader with(Context appContext) {
        return with(appContext, null, null);
    }

    public static FrescoImageLoader with(Context appContext,
                                         ImagePipelineConfig imagePipelineConfig) {
        return with(appContext, imagePipelineConfig, null);
    }

    public static FrescoImageLoader with(Context appContext,
                                         ImagePipelineConfig imagePipelineConfig, DraweeConfig draweeConfig) {
        Fresco.initialize(appContext, imagePipelineConfig, draweeConfig);
        return new FrescoImageLoader(appContext);
    }

    @Override
    public void downloadImage(Uri uri, final int position, final Callback callback) {
//        setCallback(callback);
        ImageRequest request = ImageRequest.fromUri(uri);

        File localCache = getCacheFile(request);
        if (localCache.exists()) {
            callback.onCacheHit(position, localCache);
        } else {
            callback.onStart(position); // ensure `onStart` is called before `onProgress` and `onFinish`
            callback.onProgress(position, 0); // show 0 progress immediately

            ImagePipeline pipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<PooledByteBuffer>> source
                    = pipeline.fetchEncodedImage(request, true);
            sourceList.add(source);
            source.subscribe(new ImageDownloadSubscriber(mAppContext) {
                @Override
                protected void onProgress(int progress) {
//                    Message msg = Message.obtain();
//                    msg.what = ImageLoader.STATUS_PROGRESS;
//                    msg.obj = progress;
//                    postMessage(msg);
                    callback.onProgress(position, progress);
                }

                @Override
                protected void onSuccess(File image) {
//                    Message finishMsg = Message.obtain();
//                    finishMsg.what = ImageLoader.STATUS_FINISH;
//                    postMessage(finishMsg);
                    callback.onFinish(position);

//                    Message missMsg = Message.obtain();
//                    missMsg.what = ImageLoader.STATUS_CACHE_MISS;
//                    missMsg.obj = image;
//                    postMessage(missMsg);
                    callback.onCacheMiss(position, image);
                }

                @Override
                protected void onFail(Throwable t) {
                    t.printStackTrace();
                }
            }, mExecutorSupplier.forBackgroundTasks());
        }
    }

    @Override
    public void loadImage(File image, ImageView imageView) {
//        Fresco.getImagePipeline().
    }

    @Override
    public void cancel() {
        for (DataSource<CloseableReference<PooledByteBuffer>> source : sourceList) {
            source.close();
        }
    }

    @Override
    public View showThumbnail(TransferWindow parent, Uri thumbnail, int scaleType) {
        SimpleDraweeView thumbnailView = new SimpleDraweeView(parent.getContext());
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(thumbnail)
                .build();
//        switch (scaleType) {
//            case BigImageView.INIT_SCALE_TYPE_CENTER_CROP:
//                thumbnailView.getHierarchy()
//                        .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
//                break;
//            case BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE:
//                thumbnailView.getHierarchy()
//                        .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
//            default:
//                break;
//        }
        thumbnailView.setController(controller);
        return thumbnailView;
    }

    @Override
    public void prefetch(Uri uri) {
        ImagePipeline pipeline = Fresco.getImagePipeline();
        pipeline.prefetchToDiskCache(ImageRequest.fromUri(uri),
                false); // we don't need context, but avoid null
    }

    private File getCacheFile(final ImageRequest request) {
        FileCache mainFileCache = ImagePipelineFactory
                .getInstance()
                .getMainFileCache();
        final CacheKey cacheKey = DefaultCacheKeyFactory
                .getInstance()
                .getEncodedCacheKey(request, false); // we don't need context, but avoid null
        File cacheFile = request.getSourceFile();
        // http://crashes.to/s/ee10638fb31
        if (mainFileCache.hasKey(cacheKey) && mainFileCache.getResource(cacheKey) != null) {
            cacheFile = ((FileBinaryResource) mainFileCache.getResource(cacheKey)).getFile();
        }
        return cacheFile;
    }
}
