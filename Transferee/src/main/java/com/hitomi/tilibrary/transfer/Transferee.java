package com.hitomi.tilibrary.transfer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.loader.NoneImageLoader;
import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;

/**
 * Main workflow: <br/>
 * 1、点击缩略图展示缩略图到 transferee 过渡动画 <br/>
 * 2、显示下载高清图片进度 <br/>
 * 3、加载完成显示高清图片 <br/>
 * 4、高清图支持手势缩放 <br/>
 * 5、关闭 transferee 展示 transferee 到原缩略图的过渡动画 <br/>
 * Created by hitomi on 2017/1/19.
 * <p>
 * email: 196425254@qq.com
 */
public class Transferee implements DialogInterface.OnShowListener,
        DialogInterface.OnKeyListener,
        TransferLayout.OnLayoutResetListener {

    static volatile Transferee defaultInstance;

    private Context context;
    private Dialog transDialog;

    private TransferLayout transLayout;
    private TransferConfig transConfig;
    private OnTransfereeStateChangeListener transListener;

    // 因为Dialog的关闭有动画延迟，固不能使用 dialog.isShowing, 去判断 transferee 的显示逻辑
    private boolean shown;

    /**
     * 构造方法私有化，通过{@link #getDefault(Context)} 创建 transferee
     *
     * @param context 上下文环境
     */
    private Transferee(Context context) {
        this.context = context;
        creatLayout();
        createDialog();
    }

    /**
     * @param context
     * @return {@link Transferee}
     */
    public static Transferee getDefault(Context context) {
        if (defaultInstance == null) {
            synchronized (Transferee.class) {
                if (defaultInstance == null) {
                    defaultInstance = new Transferee(context);
                }
            }
        }
        return defaultInstance;
    }

    private void creatLayout() {
        transLayout = new TransferLayout(context);
        transLayout.setOnLayoutResetListener(this);
    }

    private void createDialog() {
        transDialog = new AlertDialog.Builder(context, getDialogStyle())
                .setView(transLayout)
                .create();
        transDialog.setOnShowListener(this);
        transDialog.setOnKeyListener(this);
    }

    /**
     * 兼容4.4以下的全屏 Dialog 样式
     *
     * @return The style of the dialog
     */
    private int getDialogStyle() {
        int dialogStyle;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            dialogStyle = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen;
        } else {
            dialogStyle = android.R.style.Theme_Translucent_NoTitleBar;
        }
        return dialogStyle;
    }

    /**
     * 检查参数，如果必须参数缺少，就使用缺省参数或者抛出异常
     */
    private void checkConfig() {
        if (transConfig.isSourceEmpty())
            throw new IllegalArgumentException("the parameter sourceImageList can't be empty");

        transConfig.setNowThumbnailIndex(transConfig.getNowThumbnailIndex() < 0
                ? 0 : transConfig.getNowThumbnailIndex());

        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());

        transConfig.setDuration(transConfig.getDuration() <= 0
                ? 300 : transConfig.getDuration());

        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressBarIndicator() : transConfig.getProgressIndicator());

        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new CircleIndexIndicator() : transConfig.getIndexIndicator());

        transConfig.setImageLoader(transConfig.getImageLoader() == null
                ? new NoneImageLoader() : transConfig.getImageLoader());
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
            checkConfig();
            transLayout.apply(config);
        }
        return defaultInstance;
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
        if (shown) return;
        transDialog.show();
        if (transListener != null)
            transListener.onShow();

        shown = true;
    }

    /**
     * 显示 transferee, 并设置 OnTransfereeChangeListener
     *
     * @param listener {@link OnTransfereeStateChangeListener}
     */
    public void show(OnTransfereeStateChangeListener listener) {
        if (shown) return;
        transDialog.show();
        transListener = listener;
        transListener.onShow();

        shown = true;
    }

    /**
     * 关闭 transferee
     */
    public void dismiss() {
        if (!shown) return;
        transLayout.dismiss(transConfig.getNowThumbnailIndex());
        shown = false;
    }

    /**
     * 销毁 transferee 组件
     */
    public void destroy() {
        defaultInstance = null;
    }

    /**
     * 清除 transferee 缓存
     */
    public static void clear(ImageLoader imageLoader) {
        imageLoader.clearCache();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        transLayout.show();
    }

    @Override
    public void onReset() {
        transDialog.dismiss();
        if (transListener != null)
            transListener.onDismiss();

        shown = false;
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP &&
                !event.isCanceled()) {
            dismiss();
        }
        return true;
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
     * Transferee 显示的时候调用 {@link OnTransfereeStateChangeListener#onShow()}
     * <p>
     * Transferee 关闭的时候调用 {@link OnTransfereeStateChangeListener#onDismiss()}
     */
    public interface OnTransfereeStateChangeListener {
        void onShow();

        void onDismiss();
    }

}
