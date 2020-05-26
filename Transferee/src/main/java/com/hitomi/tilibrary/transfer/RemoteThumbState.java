package com.hitomi.tilibrary.transfer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;
import java.util.List;

/**
 * 用户指定了缩略图路径，使用该路径加载缩略图，
 * 并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by Vans Z on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
@Deprecated
class RemoteThumbState extends TransferState {

    RemoteThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, int position) {
        final TransferConfig config = transfer.getTransConfig();

        ImageLoader imageLoader = config.getImageLoader();
        String imgUrl = config.getThumbnailImageList().get(position);

        if (imageLoader.getCache(imgUrl) != null) {
            imageLoader.showImage(imgUrl, transImage,
                    config.getMissDrawable(transfer.getContext()), null);
        } else {
            transImage.setImageDrawable(config.getMissDrawable(transfer.getContext()));
        }
    }

    @Override
    public TransferImage transferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position), true);
        transformThumbnail(config.getThumbnailImageList().get(position), transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferConfig config = transfer.getTransConfig();
        final TransferImage targetImage = transfer.transAdapter.getImageItem(position);
        final ImageLoader imageLoader = config.getImageLoader();

        if (config.isJustLoadHitPage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪且设置了占位图， 所以这里直接加载原图即可
            loadSourceImage(targetImage.getDrawable(), position, targetImage);
        } else {
            String thumbUrl = config.getThumbnailImageList().get(position);

            if (imageLoader.getCache(thumbUrl) != null) {
                imageLoader.loadImageAsync(thumbUrl, new ImageLoader.ThumbnailCallback() {

                    @Override
                    public void onFinish(Bitmap bitmap) {
                        Drawable placeholder = null;
                        if (bitmap == null)
                            placeholder = config.getMissDrawable(transfer.getContext());
                        else
                            placeholder = new BitmapDrawable(transfer.getContext().getResources(), bitmap);

                        loadSourceImage(placeholder, position, targetImage);
                    }
                });
            } else {
                loadSourceImage(config.getMissDrawable(transfer.getContext()),
                        position, targetImage);
            }
        }
    }

    private void loadSourceImage(Drawable drawable, final int position, final TransferImage targetImage) {
        final TransferConfig config = transfer.getTransConfig();
        final ImageLoader imageLoader = config.getImageLoader();
        final String sourceUrl = config.getSourceUrlList().get(position);
        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, transfer.transAdapter.getParentItem(position));
        imageLoader.showImage(sourceUrl, targetImage, drawable, new ImageLoader.SourceCallback() {

            @Override
            public void onStart() {
                progressIndicator.onStart(position);
            }

            @Override
            public void onProgress(int progress) {
                progressIndicator.onProgress(position, progress);
            }

            @Override
            public void onDelivered(int status, File source) {
                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像
                switch (status) {
                    case ImageLoader.STATUS_DISPLAY_SUCCESS:
                        startPreview(targetImage, source, sourceUrl, position);
                        break;
                    case ImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                        targetImage.setImageDrawable(config.getErrorDrawable(transfer.getContext()));
                        break;
                }
            }
        });
    }

    @Override
    public TransferImage transferOut(final int position) {
        TransferImage transImage = null;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();
        if (position <= originImageList.size() - 1 && originImageList.get(position) != null) {
            transImage = createTransferImage(originImageList.get(position), true);
            transformThumbnail(config.getThumbnailImageList().get(position), transImage, false);
            transfer.addView(transImage, 1);
        }
        return transImage;
    }
}
