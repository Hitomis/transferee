package com.hitomi.tilibrary.transfer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;

/**
 * 没有缩略图 ImageView, 直接在 transferee 中展示图片
 * <p>
 * Created by Vans Z on 2020/4/16.
 * <p>
 * email: 196425254@qq.com
 */
public class NoneThumbState extends TransferState {

    NoneThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(TransferImage transImage, int position) {
        // 在此种状态下无需处理 prepareTransfer
    }

    @Override
    public TransferImage transferIn(int position) {
        transfer.displayTransfer();
        return null;
    }

    @Override
    public void transferLoad(final int position) {
        TransferAdapter adapter = transfer.transAdapter;
        final TransferConfig transConfig = transfer.getTransConfig();
        final String imgUrl = transConfig.getSourceUrlList().get(position);
        final TransferImage targetImage = adapter.getImageItem(position);

        File cache = transConfig.getImageLoader().getCache(imgUrl);
        if (cache == null) {
            Drawable placeholder = transConfig.getMissDrawable(transfer.getContext());
            // 按缺省的 drawable 大小裁剪初始显示的图片区域
            int[] clipSize = new int[]{placeholder.getIntrinsicWidth(), placeholder.getIntrinsicHeight()};
            clipTargetImage(targetImage, placeholder, clipSize);
            IProgressIndicator progressIndicator = transConfig.getProgressIndicator();
            progressIndicator.attach(position, adapter.getParentItem(position));
            targetImage.setImageDrawable(placeholder);
            loadSourceImage(targetImage, progressIndicator, imgUrl, position);
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(cache.getAbsolutePath());
            if (bitmap == null) {
                targetImage.setImageDrawable(transConfig.getMissDrawable(transfer.getContext()));
            } else {
                targetImage.setImageBitmap(bitmap);
            }
            loadSourceImage(targetImage, null, imgUrl, position);
        }
    }

    private void loadSourceImage(final TransferImage targetImage, final IProgressIndicator progressIndicator,
                                 final String imgUrl, final int position) {
        final TransferConfig transConfig = transfer.getTransConfig();
        transConfig.getImageLoader().loadSource(imgUrl, new ImageLoader.SourceCallback() {

            @Override
            public void onStart() {
                if (progressIndicator != null)
                    progressIndicator.onStart(position);
            }

            @Override
            public void onProgress(int progress) {
                if (progressIndicator != null)
                    progressIndicator.onProgress(position, progress);
            }

            @Override
            public void onDelivered(final int status, final File source) {
                switch (status) {
                    case ImageLoader.STATUS_DISPLAY_SUCCESS: // 加载成功
                        startPreview(targetImage, source, imgUrl, new StartPreviewCallback() {
                            @Override
                            public void invoke() {
                                if (progressIndicator != null)
                                    progressIndicator.onFinish(position);
                                if (progressIndicator != null)
                                    targetImage.transformIn();
                            }
                        });
                        break;
                    case ImageLoader.STATUS_DISPLAY_FAILED: // 加载失败，显示加载错误的占位图
                        loadFailedDrawable(targetImage, position);
                        break;
                }
            }
        });
    }

    @Override
    public TransferImage transferOut(int position) {
        // 因为没有缩略图 ImageView ，直接返回null,执行扩散动画
        return null;
    }
}
