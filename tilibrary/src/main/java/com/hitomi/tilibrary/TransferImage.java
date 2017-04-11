package com.hitomi.tilibrary;

import android.content.Context;
import android.graphics.Color;

import com.hitomi.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;

/**
 * Main workflow: <br/>
 * 1、点击缩略图展示缩略图到 TransferImage 过渡动画 <br/>
 * 2、显示下载高清图片进度 <br/>
 * 3、加载完成显示高清图片 <br/>
 * 4、高清图支持手势缩放 <br/>
 * 5、关闭 TransferImage 展示 TransferImage 到原缩略图的过渡动画 <br/>
 * Created by hitomi on 2017/1/19.
 */
public class TransferImage {

    static volatile TransferImage defaultInstance;

    private Context context;
    private TransferLayout transLayout;
    private TransferConfig transConfig;

    /**
     * 构造方法私有化，通过{@link #getDefault(Context)} 创建 TransferImage
     *
     * @param context 上下文环境
     */
    private TransferImage(Context context) {
        this.context = context;
        this.transLayout = new TransferLayout(context);
    }

    /**
     * @param context
     * @return
     */
    public static TransferImage getDefault(Context context) {
        if (defaultInstance == null) {
            synchronized (TransferImage.class) {
                if (defaultInstance == null) {
                    defaultInstance = new TransferImage(context);
                }
            }
        }
        return defaultInstance;
    }

    private void checkConfig() {
        transConfig.setNowThumbnailIndex(transConfig.getNowThumbnailIndex() < 0
                ? 0 : transConfig.getNowThumbnailIndex());

        transConfig.setBackgroundColor(transConfig.getBackgroundColor() == 0
                ? Color.BLACK : transConfig.getBackgroundColor());

        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());

        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressPieIndicator() : transConfig.getProgressIndicator());

        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new CircleIndexIndicator() : transConfig.getIndexIndicator());

        transConfig.setImageLoader(transConfig.getImageLoader() == null
                ? GlideImageLoader.with(context) : transConfig.getImageLoader());

        if (transConfig.getSourceImageList() == null || transConfig.getSourceImageList().isEmpty())
            transConfig.setSourceImageList(transConfig.getThumbnailImageList());

        if (transConfig.getThumbnailImageList() == null || transConfig.getThumbnailImageList().isEmpty())
            transConfig.setThumbnailImageList(transConfig.getSourceImageList());
    }

    public TransferImage apply(TransferConfig config) {
        if (!transLayout.isAdded()) {
            transConfig = config;
            checkConfig();
            transLayout.apply(config);
        }
        return defaultInstance;
    }

    /**
     * TransferImage 是否显示
     *
     * @return true ：显示, false ：关闭
     */
    public boolean isShown() {
        return transLayout.isAdded();
    }

    /**
     * 显示 TransferImage
     */
    public void show() {
        if (transLayout.isAdded()) return;
        transLayout.show();

    }

    /**
     * 关闭 TransferImage
     */
    public void dismiss() {
        if (!transLayout.isAdded()) return;
        transLayout.dismiss(transConfig.getNowShowIndex());
    }

    public void destroy() {
        defaultInstance = null;
    }

}
