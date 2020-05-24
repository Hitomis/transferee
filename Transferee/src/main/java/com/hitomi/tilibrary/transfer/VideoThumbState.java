package com.hitomi.tilibrary.transfer;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.utils.EncryptUtils;
import com.hitomi.tilibrary.utils.FileUtils;
import com.hitomi.tilibrary.view.image.TransferImage;
import com.vansz.exoplayer.ExoVideoView;

import java.io.File;
import java.util.List;

/**
 * InAnimate: transInImage 伸展的同时，透明度降低到0; 视频开始播放并且透明度由0到1
 * OutAnimate: transOutImage 缩小同时，透明度由0到1; 视频停止播放透明度由1到0
 * Created by Vans Z on 2020/5/19.
 */
public class VideoThumbState extends TransferState {
    static final String FRAME_DIR = "frame";

    VideoThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(TransferImage transImage, int position) {
        // 在此种状态下无需处理 prepareTransfer
    }

    @Override
    public TransferImage transferIn(int position) {
        TransferImage transInImage = null;
        TransferConfig transConfig = transfer.getTransConfig();
        ImageView originImage = transConfig.getOriginImageList().get(position);
        String videoSourceUrl = transConfig.getSourceImageList().get(position);
        if (originImage.getDrawable() == null) { // 没有占位图并且视频指定帧还没有加载好
            transfer.displayTransfer();
        } else {
            transInImage = createTransferImage(originImage, true);
            transInImage.setImageDrawable(originImage.getDrawable());
            transInImage.setAlpha(1f);
            transInImage.animate().alpha(0f).setDuration(transConfig.getDuration());
            transInImage.transformIn();
            transfer.addView(transInImage, 1);

            File firstFrameFile = getFirstFrameFile(videoSourceUrl);
            if (firstFrameFile.exists()) {
                TransferImage alphaOneImage = createTransferImage(originImage, false);
                alphaOneImage.setImageBitmap(BitmapFactory.decodeFile(
                        firstFrameFile.getAbsolutePath()));
                alphaOneImage.setAlpha(0f);
                alphaOneImage.animate().alpha(1f).setDuration(transConfig.getDuration());
                alphaOneImage.transformIn();
                transfer.addView(alphaOneImage, 2);
            }
        }
        return transInImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferAdapter transAdapter = transfer.transAdapter;
        final TransferConfig transConfig = transfer.getTransConfig();
        final String videoSourceUrl = transConfig.getSourceImageList().get(position);
        final ExoVideoView exoVideo = transAdapter.getVideoItem(position);
        exoVideo.setVideoStateChangeListener(new ExoVideoView.VideoStateChangeListener() {
            private IProgressIndicator progressIndicator = transConfig.getProgressIndicator();
            private boolean isAttachProgress = false;

            @Override
            public void onVideoBuffering() {
                if (isAttachProgress) return;
                isAttachProgress = true;
                progressIndicator.attach(position, transfer.transAdapter.getParentItem(position));
                progressIndicator.onStart(position);
            }

            @Override
            public void onVideoReady() {
                progressIndicator.onFinish(position);
            }

            @Override
            public void onVideoRendered() {
                File firstFrameFile = getFirstFrameFile(videoSourceUrl);
                if (firstFrameFile.exists()) {
                    // 首帧图片存在说明在 transferIn 方法中创建了两个 TransferImage 用来完成过渡动画
                    View alphaOneImage = transfer.getChildAt(2);
                    if (alphaOneImage instanceof TransferImage)
                        transfer.removeFromParent(alphaOneImage);
                } else {
                    FileUtils.save(exoVideo.getBitmap(), firstFrameFile);
                }
                // 最后删除 pos 1 位置的 TransferImage
                View alphaZeroImage = transfer.getChildAt(1);
                if (alphaZeroImage instanceof TransferImage)
                    transfer.removeFromParent(alphaZeroImage);
            }
        });
        exoVideo.play(transConfig.getSourceImageList().get(position), false);
    }

    private File getFirstFrameFile(String videoSourceUrl) {
        File cacheDir = new File(transfer.getContext().getCacheDir(), ExoVideoView.CACHE_DIR);
        String frameName = EncryptUtils.encryptMD5ToString(videoSourceUrl);
        return new File(cacheDir, String.format("/%s/%s.jpg", FRAME_DIR, frameName.toLowerCase()));
    }

    @Override
    public TransferImage transferOut(int position) {
        TransferImage transOutImage = null;
        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();

        if (position <= originImageList.size() - 1 && originImageList.get(position) != null) {
            ImageView originImage = originImageList.get(position);
            int[] location = new int[2];
            originImage.getLocationInWindow(location);

            TransferImage alphaOneImage = createTransferImage(originImage, true);
            alphaOneImage.setImageDrawable(originImage.getDrawable());
            alphaOneImage.setAlpha(0f);
            alphaOneImage.animate().alpha(1f).setDuration(config.getDuration());
            alphaOneImage.transformOut();

            TransferImage alphaZeroImage = createTransferImage(originImage, false);
            alphaZeroImage.setImageBitmap(transfer.getCurrentVideo().getBitmap());
            alphaZeroImage.setAlpha(1f);
            alphaZeroImage.animate().alpha(0f).setDuration(config.getDuration());
            alphaZeroImage.transformOut();

            transfer.addView(alphaOneImage, 1);
            transfer.addView(alphaZeroImage, 2);
            transOutImage = alphaOneImage;
        }
        transfer.transAdapter.getVideoItem(position).pause();
        return transOutImage;
    }

}
