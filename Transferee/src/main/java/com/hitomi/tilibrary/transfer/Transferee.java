package com.hitomi.tilibrary.transfer;

import android.app.Application;
import android.content.Context;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.utils.AppManager;
import com.hitomi.tilibrary.utils.FileUtils;
import com.hitomi.tilibrary.view.dialog.TransferDialog;
import com.hitomi.tilibrary.view.video.ExoVideoView;
import com.hitomi.tilibrary.view.video.source.ExoSourceManager;

import java.io.File;

/**
 * Main workflow: <br/>
 * 1、点击缩略图展示缩略图到 transferee 过渡动画 <br/>
 * 2、显示下载高清图片进度 <br/>
 * 3、加载完成显示高清图片 <br/>
 * 4、高清图支持手势缩放 <br/>
 * 5、关闭 transferee 展示 transferee 到原缩略图的过渡动画 <br/>
 * Created by Vans Z on 2017/1/19.
 * <p>
 * email: 196425254@qq.com
 */
public class Transferee implements TransferLayout.OnLayoutResetListener, AppManager.OnAppStateChangeListener {

    private Context context;
    private TransferDialog transDialog;
    private TransferLayout transLayout;
    private TransferConfig transConfig;
    private OnTransfereeStateChangeListener transListener;

    // 因为Dialog的关闭有动画延迟，固不能使用 dialog.isShowing, 去判断 transferee 的显示逻辑
    private boolean shown;

    /**
     * 构造方法私有化，通过{@link #getDefault(Context)} 创建 transferee
     *
     * @param context FragmentActivity 实例上下文环境
     */
    private Transferee(Context context) {
        this.context = context;
        AppManager.getInstance().init((Application) context.getApplicationContext());
    }

    /**
     * @param context FragmentActivity 实例上下文环境
     * @return {@link Transferee}
     */
    public static Transferee getDefault(Context context) {
        return new Transferee(context);
    }

    private TransferLayout createLayout() {
        transLayout = new TransferLayout(context);
        transLayout.setOnLayoutResetListener(this);
        transLayout.apply(transConfig);
        return transLayout;
    }

    /**
     * 检查参数，如果必须参数缺少，就使用缺省参数或者抛出异常
     */
    private void checkConfig() {
        if (transConfig == null)
            throw new IllegalArgumentException("The parameter TransferConfig can't be null");
        if (transConfig.isSourceEmpty())
            throw new IllegalArgumentException("The parameter sourceUrlList or sourceUriList  can't be empty");
        if (transConfig.getImageLoader() == null)
            throw new IllegalArgumentException("Need to specify an ImageLoader");

        transConfig.setNowThumbnailIndex(Math.max(transConfig.getNowThumbnailIndex(), 0));
        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());
        transConfig.setDuration(transConfig.getDuration() <= 0
                ? 300 : transConfig.getDuration());
        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressBarIndicator() : transConfig.getProgressIndicator());
        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new CircleIndexIndicator() : transConfig.getIndexIndicator());
    }

    /**
     * 配置 transferee 参数对象
     *
     * @param config 参数对象
     * @return transferee
     */
    public Transferee apply(TransferConfig config) {
        if (!shown) {
            transConfig = config;
            OriginalViewHelper.getInstance().fillOriginImages(config);
            checkConfig();
        }
        return this;
    }

    /**
     * transferee 是否显示
     *
     * @return true ：显示, false ：关闭
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * 显示 transferee
     */
    public void show() {
        shown();
    }

    /**
     * 显示 transferee, 并设置 OnTransfereeChangeListener
     *
     * @param listener {@link OnTransfereeStateChangeListener}
     */
    public void show(OnTransfereeStateChangeListener listener) {
        transListener = listener;
        shown();
    }

    private void shown() {
        if (shown || !(context instanceof FragmentActivity)) return;
        AppManager.getInstance().register(this);
        transDialog = new TransferDialog(createLayout(), new TransferDialog.OnKeyBackListener() {
            @Override
            public void onBack() {
                dismiss();
            }
        });
        transDialog.show(((FragmentActivity) context).getSupportFragmentManager(), null);
        if (transListener != null) {
            transListener.onShow();
        }
        shown = true;
    }

    /**
     * 关闭 transferee
     */
    public void dismiss() {
        if (shown) {
            transLayout.dismiss(transConfig.getNowThumbnailIndex());
        }
    }

    @Override
    public void onReset() {
        AppManager.getInstance().unregister(this);
        transDialog.dismiss();
        if (transListener != null)
            transListener.onDismiss();
        transLayout = null;
        shown = false;
    }

    @Override
    public void onForeground() {
        transLayout.pauseOrPlayVideo(false);
    }

    @Override
    public void onBackground() {
        transLayout.pauseOrPlayVideo(true);
    }

    /**
     * 获取图片文件
     */
    public File getImageFile(String imageUrl) {
        return transConfig.getImageLoader().getCache(imageUrl);
    }

    /**
     * 清除 transferee 缓存,包括图片和视频文件缓存，注意清除视频缓存必须保证 transferee 是关闭状态
     */
    public void clear() {
        if (transConfig != null && transConfig.getImageLoader() != null) {
            transConfig.getImageLoader().clearCache();
        }
        File cacheFile = new File(context.getCacheDir(), ExoVideoView.CACHE_DIR);
        if (cacheFile.exists() && !shown) {
            FileUtils.deleteDir(new File(cacheFile, VideoThumbState.FRAME_DIR));
            ExoSourceManager.clearCache(context, cacheFile, null);
        }
    }

    /**
     * 设置 Transferee 显示和关闭的监听器
     *
     * @param listener {@link OnTransfereeStateChangeListener}
     */
    public void setOnTransfereeStateChangeListener(OnTransfereeStateChangeListener listener) {
        transListener = listener;
    }

    /**
     * 资源销毁，防止内存泄漏
     */
    public void destroy() {
        if (transConfig != null) {
            transConfig.destroy();
            transConfig = null;
        }
        context = null;
        transLayout = null;
        transDialog = null;
        transListener = null;
    }

    /**
     * Transferee 显示的时候调用 {@link OnTransfereeStateChangeListener#onShow()}
     * <p>
     * Transferee 关闭的时候调用 {@link OnTransfereeStateChangeListener#onDismiss()}
     */
    public interface OnTransfereeStateChangeListener {
        void onShow();

        void onDismiss();
    }

    public interface OnTransfereeLongClickListener {
        void onLongClick(ImageView imageView, String imageUri, int pos);
    }

}
