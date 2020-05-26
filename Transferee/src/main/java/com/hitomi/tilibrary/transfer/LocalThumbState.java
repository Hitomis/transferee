package com.hitomi.tilibrary.transfer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;
import java.util.List;

/**
 * 高清图图片已经加载过了，使用高清图作为缩略图。
 * 同时使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by Vans Z on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
class LocalThumbState extends TransferState {

    LocalThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {
        final TransferConfig config = transfer.getTransConfig();
        ImageLoader imageLoader = config.getImageLoader();
        String imgUrl = config.getSourceUrlList().get(position);
        imageLoader.showImage(imgUrl, transImage, config.getMissDrawable(transfer.getContext()), null);
    }

    @Override
    public TransferImage transferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position), true);
        transformThumbnail(config.getSourceUrlList().get(position), transImage, true);
        transfer.addView(transImage, 1);
        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceUrlList().get(position);
        final TransferImage targetImage = transfer.transAdapter.getImageItem(position);

        if (config.isJustLoadHitPage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪且设置了占位图， 所以这里直接加载原图即可
            loadSourceImage(imgUrl, targetImage, targetImage.getDrawable(), position);
        } else {
            config.getImageLoader().loadImageAsync(imgUrl, new ImageLoader.ThumbnailCallback() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    Drawable placeholder = null;
                    if (bitmap == null)
                        placeholder = config.getMissDrawable(transfer.getContext());
                    else
                        placeholder = new BitmapDrawable(transfer.getContext().getResources(), bitmap);

                    loadSourceImage(imgUrl, targetImage, placeholder, position);
                }
            });
        }
    }

    private void loadSourceImage(final String imgUrl, final TransferImage targetImage, Drawable drawable, final int position) {
        final TransferConfig config = transfer.getTransConfig();

        config.getImageLoader().showImage(imgUrl, targetImage, drawable, new ImageLoader.SourceCallback() {

            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onDelivered(int status, File source) {
                switch (status) {
                    case ImageLoader.STATUS_DISPLAY_SUCCESS:
                        if (TransferImage.STATE_TRANS_CLIP == targetImage.getState())
                            targetImage.transformIn(TransferImage.STAGE_SCALE);
                        startPreview(targetImage, source, imgUrl, position);
                        break;
                    case ImageLoader.STATUS_DISPLAY_CANCEL:
                        if (targetImage.getDrawable() != null) {
                            startPreview(targetImage, source, imgUrl, position);
                        }
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
            transformThumbnail(config.getSourceUrlList().get(position), transImage, false);
            transfer.addView(transImage, 1);
        }
        return transImage;
    }
}
