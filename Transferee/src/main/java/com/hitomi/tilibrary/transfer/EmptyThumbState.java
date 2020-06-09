package com.hitomi.tilibrary.transfer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;
import java.util.List;

/**
 * 高清图尚未加载，使用原 ImageView 中显示的图片作为缩略图。
 * 同时使用 {@link TransferImage#CATE_ANIMA_APART} 动画类型展示图片
 * <p>
 * Created by Vans Z on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
class EmptyThumbState extends TransferState {

    EmptyThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {
        transImage.setImageDrawable(clipAndGetPlaceHolder(transImage, position));
    }

    @Override
    public TransferImage transferIn(final int position) {
        ImageView originImage = transfer.getTransConfig()
                .getOriginImageList().get(position);

        TransferImage transImage = createTransferImage(originImage, true);
        transImage.setImageDrawable(originImage.getDrawable());
        transImage.transformIn(TransferImage.STAGE_TRANSLATE);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        TransferAdapter adapter = transfer.transAdapter;
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceUrlList().get(position);
        final TransferImage targetImage = adapter.getImageItem(position);

        Drawable placeHolder;
        if (config.isJustLoadHitPage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪过了， 所以只需要获取 Drawable 作为占位图即可
            placeHolder = getPlaceHolder(position);
        } else {
            placeHolder = clipAndGetPlaceHolder(targetImage, position);
        }
        targetImage.setImageDrawable(placeHolder);
        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        config.getImageLoader().loadSource(imgUrl, new ImageLoader.SourceCallback() {

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
                switch (status) {
                    case ImageLoader.STATUS_DISPLAY_SUCCESS: // 加载成功
                        startPreview(targetImage, source, imgUrl, new StartPreviewCallback() {
                            @Override
                            public void invoke() {
                                progressIndicator.onFinish(position);
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
            Drawable thumbnailDrawable = transfer.transAdapter.getImageItem(
                    config.getNowThumbnailIndex()).getDrawable();
            transImage.setImageDrawable(thumbnailDrawable);
            transImage.transformOut(TransferImage.STAGE_TRANSLATE);

            transfer.addView(transImage, 1);
        }

        return transImage;
    }

    /**
     * 获取 position 位置处的 占位图，如果 position 超出下标，获取 MissDrawable
     *
     * @param position 图片索引
     * @return 占位图
     */
    private Drawable getPlaceHolder(int position) {
        Drawable placeHolder = null;

        TransferConfig config = transfer.getTransConfig();
        ImageView originImage = config.getOriginImageList().get(position);
        if (originImage != null) {
            placeHolder = originImage.getDrawable();
        }
        if (placeHolder == null) {
            placeHolder = config.getMissDrawable(transfer.getContext());
        }
        return placeHolder;
    }

    /**
     * 裁剪用于显示 PlaceHolder 的 TransferImage
     *
     * @param targetImage 被裁剪的 TransferImage
     * @param position    图片索引
     * @return 被裁减的 TransferImage 中显示的 Drawable
     */
    private Drawable clipAndGetPlaceHolder(TransferImage targetImage, int position) {
        Drawable placeHolder = getPlaceHolder(position);
        clipTargetImage(targetImage, placeHolder,
                getPlaceholderClipSize(position, TYPE_PLACEHOLDER_MISS));
        return placeHolder;
    }
}
