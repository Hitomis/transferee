package com.hitomi.yifangbao.tilibrary.loader.fresco;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferInputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public abstract class ImageDownloadSubscriber extends BaseDataSubscriber<CloseableReference<PooledByteBuffer>> {
    private final File mTempFile;
    private volatile boolean mFinished;

    public ImageDownloadSubscriber(Context context) {
        mTempFile = new File(context.getCacheDir(), "" + System.currentTimeMillis() + ".png");
    }

    @Override
    public void onProgressUpdate(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (!mFinished) {
            onProgress((int) (dataSource.getProgress() * 100));
        }
    }

    @Override
    protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (!dataSource.isFinished() || dataSource.getResult() == null) {
            return;
        }

        // if we try to retrieve image file by cache key, it will return null
        // so we need to create a temp file, little bit hack :(
        PooledByteBufferInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new PooledByteBufferInputStream(dataSource.getResult().get());
            outputStream = new FileOutputStream(mTempFile);
            IOUtils.copy(inputStream, outputStream);

            mFinished = true;
            onSuccess(mTempFile);
        } catch (IOException e) {
            onFail(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        mFinished = true;
        onFail(new RuntimeException("onFailureImpl"));
    }

    @WorkerThread
    protected abstract void onProgress(int progress);

    @WorkerThread
    protected abstract void onSuccess(File image);

    @WorkerThread
    protected abstract void onFail(Throwable t);
}
