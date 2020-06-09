package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.loader.ImageProcessor;
import com.hitomi.tilibrary.utils.ImageUtils;
import com.hitomi.tilibrary.view.image.TransferImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;
import static com.hitomi.tilibrary.utils.ImageUtils.TYPE_GIF;

/**
 * 由于用户配置的参数不同 (例如图片是否加载过、当前播放的是不是视频有、没有绑定 View 等) <br/>
 * 使得 Transferee 所表现的行为不同，所以采用一组策略算法来实现以下不同的功能：
 * <ul>
 * <li>1. 图片进入 Transferee 的过渡动画</li>
 * <li>2. 图片加载时不同的表现形式</li>
 * <li>3. 图片从 Transferee 中出去的过渡动画</li>
 * </ul>
 * Created by Vans Z on 2020/5/4.
 * <p>
 * email: 196425254@qq.com
 */
abstract class TransferState {
    static final int TYPE_PLACEHOLDER_MISS = 1;
    static final int TYPE_PLACEHOLDER_ERROR = 2;

    protected TransferLayout transfer;

    TransferState(TransferLayout transfer) {
        this.transfer = transfer;
    }

    /**
     * 获取 View 在屏幕坐标系中的坐标
     *
     * @param view 需要定位位置的 View
     * @return 坐标系数组
     */
    private int[] getViewLocation(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        location[1] -= transfer.getPaddingTop();
        return location;
    }

    /**
     * 依据 originImage 在屏幕中的坐标和宽高信息创建一个 TransferImage
     *
     * @param originImage  缩略图 ImageView
     * @param bindListener 是否需要绑定伸缩动画执行完成监听器
     * @return TransferImage
     */
    @NonNull
    TransferImage createTransferImage(ImageView originImage, boolean bindListener) {
        TransferConfig config = transfer.getTransConfig();
        int[] location = getViewLocation(originImage);

        TransferImage transImage = new TransferImage(transfer.getContext());
        transImage.setScaleType(FIT_CENTER);
        transImage.setOriginalInfo(location[0], location[1],
                originImage.getWidth(), originImage.getHeight());
        transImage.setDuration(config.getDuration());
        transImage.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        if (bindListener)
            transImage.setOnTransferListener(transfer.transListener);

        return transImage;
    }

    /**
     * 图片加载完毕，开启预览
     *
     * @param targetImage 预览图片
     * @param imgUrl      图片url
     */
    void startPreview(final TransferImage targetImage, final File source,
                      final String imgUrl, final StartPreviewCallback callback) {
        targetImage.enableGesture();// 启用 TransferImage 的手势缩放功能
        final File cacheDir = transfer.getTransConfig().getImageLoader().getCacheDir();
        ImageProcessor.getInstance().process(transfer.getContext(), imgUrl, source,
                cacheDir, getDisplaySize(), new ImageProcessor.ImageProcessCallback() {
                    @Override
                    public void onSuccess(File file) {
                        if (ImageUtils.getImageType(file) == TYPE_GIF) {
                            try {
                                targetImage.setImageDrawable(new GifDrawable(file.getPath()));
                            } catch (IOException ignored) {
                            }
                        } else {
                            targetImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        }
                        callback.invoke();
                    }

                    @Override
                    public void onFailure() {
                        callback.invoke();
                    }
                });
    }

    /**
     * 裁剪 ImageView 显示图片的区域
     *
     * @param targetImage    被裁减的 ImageView
     * @param originDrawable 缩略图 Drawable
     * @param clipSize       裁剪的尺寸数组
     */
    void clipTargetImage(TransferImage targetImage, Drawable originDrawable, int[] clipSize) {
        Point screenSize = getDisplaySize();
        int width = screenSize.x;
        int height = screenSize.y;

        targetImage.setOriginalInfo(
                originDrawable,
                clipSize[0], clipSize[1],
                width, height);
        targetImage.transClip();
    }


    /**
     * 加载失败，显示 errorDrawable
     *
     * @param targetImage 显示 errorDrawable 的目标 View
     */
    void loadFailedDrawable(TransferImage targetImage, int pos) {
        Drawable errorDrawable = transfer.getTransConfig()
                .getErrorDrawable(transfer.getContext());
        clipTargetImage(targetImage, errorDrawable,
                getPlaceholderClipSize(pos, TYPE_PLACEHOLDER_ERROR));
        targetImage.setImageDrawable(errorDrawable);
    }

    /**
     * 先取 position 位置的 originImage, 如果为空，则从 originImageList 中
     * 找到第一个不为空的 originImage, 如果仍然找不到，则默认取缺省占位图的尺寸
     *
     * @param placeholderType TYPE_PLACEHOLDER_MISS 是缺省占位图，
     *                        TYPE_PLACEHOLDER_ERROR 是加载失败或者错误的占位图
     */
    int[] getPlaceholderClipSize(int position, int placeholderType) {
        int[] clipSize = new int[2];
        TransferConfig transConfig = transfer.getTransConfig();
        List<ImageView> originImageList = transConfig.getOriginImageList();
        ImageView originImage = originImageList.isEmpty() ? null : originImageList.get(position);
        if (originImage == null) {
            for (ImageView imageView : originImageList) {
                if (imageView != null) {
                    originImage = imageView;
                    break;
                }
            }
        }
        if (originImage == null) {
            Drawable defaultDrawable;
            if (placeholderType == TYPE_PLACEHOLDER_MISS) {
                defaultDrawable = transConfig.getMissDrawable(transfer.getContext());
            } else {
                defaultDrawable = transConfig.getErrorDrawable(transfer.getContext());
            }
            clipSize[0] = defaultDrawable.getIntrinsicWidth();
            clipSize[1] = defaultDrawable.getIntrinsicHeight();
        } else {
            clipSize[0] = originImage.getWidth();
            clipSize[1] = originImage.getHeight();
        }
        return clipSize;
    }

    /**
     * @return 可用来显示的屏幕宽高
     */
    private Point getDisplaySize() {
        WindowManager wm = (WindowManager) transfer.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return new Point();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        point.y = point.y - transfer.getPaddingTop() - transfer.getPaddingBottom();
        return point;
    }

    /**
     * @return 校正方向的 bitmap
     */
    private Bitmap getRightOrientationBitmap(File source) {
        String sourcePath = source.getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(sourcePath);
        try {
            ExifInterface exifInfo = new ExifInterface(sourcePath);
            int orientation = exifInfo.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1f, 1f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180f);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180f);
                    matrix.postScale(-1f, 1f);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90f);
                    matrix.postScale(-1f, 1f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90f);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90f);
                    matrix.postScale(-1f, 1f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90f);
                    break;
                default:
                    return bitmap;
            }
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true
            );
            bitmap.recycle();
            return rotatedBitmap;
        } catch (Exception ignored) {
            return bitmap;
        }
    }

    /**
     * 当用户使用 justLoadHitPage 属性时，
     * 需要使用 prepareTransfer 方法提前让 ViewPager 对应
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
    public abstract TransferImage transferIn(final int position);

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

    public interface StartPreviewCallback {
        void invoke();
    }

}
