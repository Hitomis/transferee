package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.lang.reflect.Field;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * 由于用户配置的参数不同 (例如 使用不同的 ImageLoader  / 是否指定了 thumbnailImageList 参数值) <br/>
 * 使得 Transferee 所表现的行为不同，所以采用一组策略算法来实现以下不同的功能：
 * <ul>
 * <li>1. 图片进入 Transferee 的过渡动画</li>
 * <li>2. 图片加载时不同的表现形式</li>
 * <li>3. 图片从 Transferee 中出去的过渡动画</li>
 * </ul>
 * Created by hitomi on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
abstract class TransferState {

    protected TransferLayout transfer;
    protected Context context;

    TransferState(TransferLayout transfer) {
        this.transfer = transfer;
        this.context = transfer.getContext();
    }

    /**
     * 由于 4.4 以下版本状态栏不可修改，所以兼容 4.4 以下版本的全屏模式时，要去除状态栏的高度
     *
     * @param oldY
     * @return
     */
    protected int getTransImageLocalY(int oldY) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return oldY;
        }
        return oldY - getStatusBarHeight();
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    protected int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取 View 在屏幕坐标系中的坐标
     *
     * @param view 需要定位位置的 View
     * @return 坐标系数组
     */
    protected int[] getViewLocation(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location;
    }

    /**
     * 依据 originImage 在屏幕中的坐标和宽高信息创建一个 TransferImage
     *
     * @param originImage 缩略图 ImageView
     * @return TransferImage
     */
    @NonNull
    protected TransferImage createTransferImage(ImageView originImage) {
        TransferConfig config = transfer.getTransConfig();
        int[] location = getViewLocation(originImage);

        TransferImage transImage = new TransferImage(context);
        transImage.setScaleType(FIT_CENTER);
        transImage.setOriginalInfo(location[0], getTransImageLocalY(location[1]),
                originImage.getWidth(), originImage.getHeight());
        transImage.setBackgroundColor(config.getBackgroundColor());
        transImage.setDuration(config.getDuration());
        transImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        transImage.setOnTransferListener(transfer.getTransListener());

        return transImage;
    }

    /**
     * 加载 imageUrl 所关联的图片到 TransferImage 并启动 TransferImage 中的过渡动画
     *
     * @param imageUrl   当前缩略图路径
     * @param transImage {@link #createTransferImage(ImageView)} 方法创建的 TransferImage
     * @param in         true : 从缩略图到高清图动画, false : 从高清图到缩略图动画
     */
    protected void transformThumbnail(String imageUrl, final TransferImage transImage, final boolean in) {
        final TransferConfig config = transfer.getTransConfig();

        ImageLoader imageLoader = config.getImageLoader();

        if (this instanceof RemoteThumState) { // RemoteThumState

            if (imageLoader.isLoaded(imageUrl)) { // 缩略图已加载过
                loadThunbnail(imageUrl, transImage, in);
            } else { // 缩略图 未加载过，则使用用户配置的缺省占位图
                transImage.setImageDrawable(config.getMissDrawable(context));
                if (in)
                    transImage.transformIn();
                else
                    transImage.transformOut();
            }

        } else { // LocalThumState
            loadThunbnail(imageUrl, transImage, in);
        }
    }

    /**
     * 加载 imageUrl 所关联的图片到 TransferImage 中
     *
     * @param imageUrl   图片路径
     * @param transImage
     * @param in         true: 表示从缩略图到 Transferee, false: 从 Transferee 到缩略图
     */
    private void loadThunbnail(String imageUrl, final TransferImage transImage, final boolean in) {
        final TransferConfig config = transfer.getTransConfig();
        ImageLoader imageLoader = config.getImageLoader();
        Drawable drawable = imageLoader.loadImageSync(imageUrl);
        if (drawable == null)
            transImage.setImageDrawable(config.getMissDrawable(context));
        else
            transImage.setImageDrawable(drawable);

        if (in)
            transImage.transformIn();
        else
            transImage.transformOut();
    }

    /**
     * 当用户使用 {@link TransferConfig#justLoadHitImage} 属
     * 性时，需要使用 prepareTransfer 方法提前让 ViewPager 对应
     * position 处的 TransferImage 剪裁并设置占位图
     *
     * @param transImage ViewPager 中 position 位置处的 TransferImage
     * @param position   当前点击的图片索引
     */
    public abstract void prepareTransfer(TransferImage transImage, final int position);

    /**
     * 创建一个 TransferImage 放置在 Transferee 中指定位置，并播放从缩略图到 Transferee 的过渡动画
     *
     * @param position 进入到 Transferee 之前，用户在图片列表中点击的图片的索引
     * @return 创建的 TransferImage
     */
    public abstract TransferImage createTransferIn(final int position);

    /**
     * 从网络或者从 {@link ImageLoader} 指定的缓存中加载 SourceImageList.get(position) 对应的图片
     *
     * @param position 原图片路径索引
     */
    public abstract void transferLoad(final int position);

    /**
     * 创建一个 TransferImage 放置在 Transferee 中指定位置，并播放从 Transferee 到 缩略图的过渡动画
     *
     * @param position 当前点击的图片索引
     * @return 创建的 TransferImage
     */
    public abstract TransferImage transferOut(final int position);

}
