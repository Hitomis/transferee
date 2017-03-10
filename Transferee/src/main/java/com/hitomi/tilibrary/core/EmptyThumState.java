package com.hitomi.tilibrary.core;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.TransferAdapter;
import com.hitomi.tilibrary.TransferConfig;
import com.hitomi.tilibrary.TransferLayout;
import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

/**
 * Created by hitomi on 2017/5/4.
 */

public class EmptyThumState extends BaseTransferState {

    public EmptyThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        ImageView originImage =  transfer.getTransConfig()
                .getOriginImageList().get(position);

        TransferImage transImage = createTransferImage(originImage);
        transImage.setImageDrawable(originImage.getDrawable());
        transImage.transformIn(TransferImage.STAGE_TRANSLATE);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void loadTransfer(final int position) {
        TransferAdapter adapter = transfer.getTransAdapter();
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceImageList().get(position);
        final TransferImage targetImage = adapter.getImageItem(position);
        ImageView originImage = config.getOriginImageList().get(position);

        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        config.getImageLoader().showSourceImage(imgUrl, targetImage,
                originImage.getDrawable(), new ImageLoader.SourceCallback() {

                    @Override
                    public void onStart() {
                        progressIndicator.onStart(position);
                    }

                    @Override
                    public void onProgress(int progress) {
                        progressIndicator.onProgress(position, progress);
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDelivered(int status) {
                        switch (status) {
                            case ImageLoader.STATUS_DISPLAY_SUCCESS: // 加载成功
                                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像
                                config.cacheLoadedImageUrl(context, imgUrl);

                                targetImage.transformIn(TransferImage.STAGE_SCALE);
                                targetImage.enable();
                                transfer.bindOnDismissListener(targetImage, position);
                                break;
                            case ImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                                targetImage.setImageDrawable(config.getErrorDrawable(context));
                                break;
                        }
                    }
                });
    }

    @Override
    public TransferImage createTransferOut(final int position) {
        TransferConfig config = transfer.getTransConfig();
        Drawable thumbnailDrawable = transfer.getTransAdapter().getImageItem(
                config.getNowThumbnailIndex()).getDrawable();

        TransferImage transImage = createTransferImage(config.getOriginImageList().get(position));
        transImage.setImageDrawable(thumbnailDrawable);
        transImage.transformOut(TransferImage.STAGE_TRANSLATE);
        transfer.addView(transImage, 1);

        return transImage;
    }


}
