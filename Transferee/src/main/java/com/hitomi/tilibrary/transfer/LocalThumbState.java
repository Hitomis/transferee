package com.hitomi.tilibrary.transfer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.utils.ImageUtils;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

import static com.hitomi.tilibrary.utils.ImageUtils.TYPE_GIF;

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

    /**
     * 加载 imageUrl 所关联的图片到 TransferImage 中
     *
     * @param imageUrl 图片路径
     * @param in       true: 表示从缩略图到 Transferee, false: 从 Transferee 到缩略图
     */
    private void loadThumbnail(String imageUrl, final TransferImage transImage, final boolean in) {
        final TransferConfig config = transfer.getTransConfig();
        ImageLoader imageLoader = config.getImageLoader();
        File thumbFile = imageLoader.getCache(imageUrl);
        Bitmap thumbBitmap = thumbFile != null ? BitmapFactory.decodeFile(thumbFile.getAbsolutePath()) : null;
        if (thumbBitmap == null)
            transImage.setImageDrawable(config.getMissDrawable(transfer.getContext()));
        else
            transImage.setImageBitmap(thumbBitmap);

        if (in)
            transImage.transformIn();
        else
            transImage.transformOut();
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {
        final TransferConfig config = transfer.getTransConfig();
        String imgUrl = config.getSourceUrlList().get(position);
        File cache = config.getImageLoader().getCache(imgUrl);
        if (cache == null) return;
        if (ImageUtils.getImageType(cache) == TYPE_GIF) {
            try {
                transImage.setImageDrawable(new GifDrawable(cache.getPath()));
            } catch (IOException ignored) {
            }
        } else {
            transImage.setImageBitmap(BitmapFactory.decodeFile(cache.getAbsolutePath()));
        }
        transImage.enableGesture();
    }

    @Override
    public TransferImage transferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();
        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position), true);
        loadThumbnail(config.getSourceUrlList().get(position), transImage, true);
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
            loadSourceImage(imgUrl, targetImage, position);
        } else {
            String filePath = config.getImageLoader().getCache(imgUrl).getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap == null) {
                targetImage.setImageDrawable(config.getMissDrawable(transfer.getContext()));
            } else {
                targetImage.setImageBitmap(bitmap);
            }
            loadSourceImage(imgUrl, targetImage, position);
        }
    }

    private void loadSourceImage(final String imgUrl, final TransferImage targetImage, final int position) {
        final TransferConfig config = transfer.getTransConfig();

        config.getImageLoader().loadSource(imgUrl, new ImageLoader.SourceCallback() {

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
                        startPreview(targetImage, source, imgUrl, new StartPreviewCallback() {
                            @Override
                            public void invoke() {
                                if (TransferImage.STATE_TRANS_CLIP == targetImage.getState())
                                    targetImage.transformIn(TransferImage.STAGE_SCALE);
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
    public TransferImage transferOut(final int position) {
        TransferImage transImage = null;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();
        if (position <= originImageList.size() - 1 && originImageList.get(position) != null) {
            transImage = createTransferImage(originImageList.get(position), true);
            loadThumbnail(config.getSourceUrlList().get(position), transImage, false);
            transfer.addView(transImage, 1);
        }
        return transImage;
    }
}
