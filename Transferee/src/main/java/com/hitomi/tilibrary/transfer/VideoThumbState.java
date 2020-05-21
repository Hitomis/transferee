package com.hitomi.tilibrary.transfer;

import android.widget.ImageView;

import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.view.image.TransferImage;
import com.vansz.exoplayer.ExoVideoView;

import java.util.List;

/**
 * InAnimate: transInImage 伸展的同时，透明度降低到0; 视频开始播放并且透明度由0到1
 * OutAnimate: transOutImage 缩小同时，透明度由0到1; 视频停止播放透明度由1到0
 * Created by Vans Z on 2020/5/19.
 */
public class VideoThumbState extends TransferState {

    VideoThumbState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(TransferImage transImage, int position) {
        // 在此种状态下无需处理 prepareTransfer
    }

    @Override
    public TransferImage createTransferIn(int position) {
        TransferImage transInImage = null;
        ImageView originImage = transfer.getTransConfig()
                .getOriginImageList().get(position);
        if (originImage.getDrawable() == null) { // 视频指定帧还没有加载好
            transfer.displayTransfer();
        } else {
            transInImage = createTransferImage(originImage);
            transInImage.setImageDrawable(originImage.getDrawable());
            transInImage.transformIn();
            transfer.addView(transInImage, 1);
        }
        return transInImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferAdapter transAdapter = transfer.transAdapter;
        final TransferConfig transConfig = transfer.getTransConfig();
        final ExoVideoView exoVideo = transAdapter.getVideoItem(position);
        exoVideo.play(transConfig.getSourceImageList().get(position));
        exoVideo.setVideoStateChangeListener(new ExoVideoView.VideoStateChangeListener() {
            private IProgressIndicator progressIndicator = transConfig.getProgressIndicator();

            @Override
            public void onVideoBuffering() {
                progressIndicator.attach(position, transAdapter.getParentItem(position));
                progressIndicator.onStart(position);
            }

            @Override
            public void onVideoReady() {
                progressIndicator.onFinish(position);
            }

            @Override
            public void onVideoRendered() {
                transfer.removeFromParent(transfer.getChildAt(1));
            }
        });
    }

    @Override
    public TransferImage transferOut(int position) {
        TransferImage transOutImage = null;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();

        if (position <= originImageList.size() - 1 && originImageList.get(position) != null) {
            ImageView originImage = originImageList.get(position);
            transOutImage = createTransferImage(originImage);
            transOutImage.setImageDrawable(originImage.getDrawable());
            transOutImage.transformOut();
            transfer.addView(transOutImage, 1);
        }
        return transOutImage;
    }
}
