package com.hitomi.tilibrary.transfer;

import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.List;

/**
 * 高清图尚未加载，使用原 ImageView 中显示的图片作为缩略图。
 * 同时使用 {@link TransferImage#CATE_ANIMA_APART} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 */
class EmptyThumState extends TransferState {

    EmptyThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {
        TransferConfig config = transfer.getTransConfig();

        Drawable placeHolder;
        int[] clipSize = new int[2];
        if (position < config.getOriginImageList().size()) {
            ImageView originImage = config.getOriginImageList().get(position);
            clipSize[0] = originImage.getWidth();
            clipSize[1] = originImage.getHeight();
            placeHolder = originImage.getDrawable();
        } else {
            placeHolder = config.getMissDrawable(context);
        }
        clipTargetImage(transImage, clipSize);

        transImage.setImageDrawable(placeHolder);
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        ImageView originImage = transfer.getTransConfig()
                .getOriginImageList().get(position);

        TransferImage transImage = createTransferImage(originImage);
        transImage.setImageDrawable(originImage.getDrawable());
        transImage.transformIn(TransferImage.STAGE_TRANSLATE);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        TransferAdapter adapter = transfer.getTransAdapter();
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceImageList().get(position);
        final TransferImage targetImage = adapter.getImageItem(position);

        Drawable placeHolder;
        int[] clipSize = new int[2];
        if (position < config.getOriginImageList().size()) {
            ImageView originImage = config.getOriginImageList().get(position);
            clipSize[0] = originImage.getWidth();
            clipSize[1] = originImage.getHeight();
            placeHolder = originImage.getDrawable();
        } else {
            placeHolder = config.getMissDrawable(context);
        }
        clipTargetImage(targetImage, clipSize);

        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        config.getImageLoader().showSourceImage(imgUrl, targetImage,
                placeHolder, new ImageLoader.SourceCallback() {

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

    /**
     * 裁剪 ImageView 显示图片的区域
     *
     * @param targetImage 被裁减的 ImageView
     * @param clipSize    裁剪的尺寸数组
     */
    private void clipTargetImage(TransferImage targetImage, int[] clipSize) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int locationX = (displayMetrics.widthPixels - clipSize[0]) / 2;
        int locationY = (getTransImageLocalY(displayMetrics.heightPixels) - clipSize[1]) / 2;
        targetImage.setOriginalInfo(locationX, locationY,
                clipSize[0], clipSize[1]);
        targetImage.transClip();
    }

    @Override
    public boolean transferOut(final int position) {
        boolean transferOut = false;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();

        if (position < originImageList.size()) {
            TransferImage transImage = createTransferImage(
                    originImageList.get(position));
            Drawable thumbnailDrawable = transfer.getTransAdapter().getImageItem(
                    config.getNowThumbnailIndex()).getDrawable();
            transImage.setImageDrawable(thumbnailDrawable);
            transImage.transformOut(TransferImage.STAGE_TRANSLATE);

            transferOut = true;
            transfer.addView(transImage, 1);
        }

        return transferOut;
    }
}
