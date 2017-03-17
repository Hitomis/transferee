package com.hitomi.tilibrary.transfer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.List;

/**
 * 高清图图片已经加载过了，使用高清图作为缩略图。
 * 同时使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 */
public class LocalThumState extends TransferState {

    public LocalThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position));
        transformThumbnail(transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceImageList().get(position);
        final TransferImage targetImage = transfer.getTransAdapter().getImageItem(position);
        final ImageLoader imageLoader = config.getImageLoader();

        imageLoader.loadThumbnailAsync(imgUrl, targetImage, new ImageLoader.ThumbnailCallback() {
            @Override
            public void onFinish(Drawable drawable) {
                if (drawable == null)
                    drawable = config.getMissDrawable(context);

                imageLoader.showSourceImage(imgUrl, targetImage, drawable, new ImageLoader.SourceCallback() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onProgress(int progress) {
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDelivered(int status) {
                        switch (status) {
                            case ImageLoader.STATUS_DISPLAY_SUCCESS:
                                // 启用 TransferImage 的手势缩放功能
                                targetImage.enable();
                                // 绑定点击关闭 Transferee
                                transfer.bindOnDismissListener(targetImage, position);
                                break;
                            case ImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                                targetImage.setImageDrawable(config.getErrorDrawable(context));
                                break;
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean transferOut(final int position) {
        boolean transferOut = false;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();

        if (position < originImageList.size()) {
            TransferImage transImage = createTransferImage(
                    originImageList.get(position));
            transformThumbnail(transImage, false);

            transferOut = true;
            transfer.addView(transImage, 1);
        }

        return transferOut;
    }
}
