package com.hitomi.tilibrary;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

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
public class Transferee implements DialogInterface.OnShowListener,
        DialogInterface.OnKeyListener,
        TransferLayout.OnLayoutResetListener {

    static volatile Transferee defaultInstance;

    private Context context;
    private TransferLayout transLayout;
    private TransferConfig transConfig;
    private Dialog transDialog;

    // 因为Dialog的关闭有动画延迟，固不能使用 dialog.isShowing, 去判断 TransferImage 的显示逻辑
    private boolean shown;

    /**
     * 构造方法私有化，通过{@link #getDefault(Context)} 创建 TransferImage
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
     * @return
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
        transDialog = new AlertDialog.Builder(context,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
                .setView(transLayout)
                .create();
        transDialog.setOnShowListener(this);
        transDialog.setOnKeyListener(this);
    }

    /**
     * 检查参数，如果必须参数缺少，就使用缺省参数
     */
    private void checkConfig() {
        transConfig.setNowThumbnailIndex(transConfig.getNowThumbnailIndex() < 0
                ? 0 : transConfig.getNowThumbnailIndex());

        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());

        transConfig.setDuration(transConfig.getDuration() <= 0
                ? 300 : transConfig.getDuration());

        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressPieIndicator() : transConfig.getProgressIndicator());

        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new CircleIndexIndicator() : transConfig.getIndexIndicator());

        transConfig.setImageLoader(transConfig.getImageLoader() == null
                ? GlideImageLoader.with(context) : transConfig.getImageLoader());

        if (transConfig.getThumbnailImageList() == null || transConfig.getThumbnailImageList().isEmpty())
            transConfig.setThumbnailImageList(transConfig.getSourceImageList());
    }

    /**
     * 配置 TransferImage 参数对象
     *
     * @param config 参数对象
     * @return TransferImage
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
     * TransferImage 是否显示
     *
     * @return true ：显示, false ：关闭
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * 显示 TransferImage
     */
    public void show() {
        if (shown) return;
        transDialog.show();
        shown = true;
    }

    /**
     * 关闭 TransferImage
     */
    public void dismiss() {
        if (!shown) return;
        transLayout.dismiss(transConfig.getNowThumbnailIndex());
        shown = false;
    }

    /**
     * 销毁 TransferImage 组件
     */
    public void destroy() {
        defaultInstance = null;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        transLayout.show();
    }

    @Override
    public void onReset() {
        transDialog.dismiss();
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


}
