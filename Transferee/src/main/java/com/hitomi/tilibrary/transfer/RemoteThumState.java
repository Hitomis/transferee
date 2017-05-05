package com.hitomi.tilibrary.transfer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.List;

/**
 * 用户指定了缩略图路径，使用该路径加载缩略图，
 * 并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 */
public class RemoteThumState extends TransferState {

    public RemoteThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position));
        transformThumbnail(config.getThumbnailImageList().get(position), transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        TransferAdapter adapter = transfer.getTransAdapter();
        final TransferConfig config = transfer.getTransConfig();
        final TransferImage targetImage = transfer.getTransAdapter().getImageItem(position);
        final ImageLoader imageLoader = config.getImageLoader();

        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        imageLoader.loadThumbnailAsync(config.getThumbnailImageList().get(position),
                targetImage, new ImageLoader.ThumbnailCallback() {

            @Override
            public void onFinish(Drawable drawable) {
                if (drawable == null)
                    drawable = config.getMissDrawable(context);

                imageLoader.showSourceImage(config.getSourceImageList().get(position),
                        targetImage, drawable, new ImageLoader.SourceCallback() {

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
                            case ImageLoader.STATUS_DISPLAY_SUCCESS:
                                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像
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
            transformThumbnail(config.getThumbnailImageList().get(position), transImage, false);

            transferOut = true;
            transfer.addView(transImage, 1);
        }

        return transferOut;
    }
}
