package com.hitomi.tilibrary.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.hitomi.tilibrary.transfer.TransferLayout;

public class TransferDialog extends BaseDialog
        implements DialogInterface.OnKeyListener, DialogInterface.OnShowListener {
    private TransferLayout transferLayout;
    private OnKeyBackListener backListener;

    public TransferDialog(TransferLayout transferLayout, OnKeyBackListener listener) {
        this.transferLayout = transferLayout;
        this.backListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return transferLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustTopAndBottom();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnShowListener(this);
            dialog.setOnKeyListener(this);
        }
    }

    /**
     * dialog 打开时的监听器
     */
    @Override
    public void onShow(DialogInterface dialog) {
        transferLayout.show();
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP
                && !event.isCanceled()
                && backListener != null) {
            backListener.onBack();
            return true;
        }
        return false;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (transferLayout.getParent() == null) return;
        ((ViewGroup) transferLayout.getParent()).removeView(transferLayout);
        backListener = null;
    }

    /**
     * 调整顶部和底部内边距
     */
    private void adjustTopAndBottom() {
        // 隐藏状态栏和导航栏，强制全屏化，不接受反驳，商业用途项目谁会用toolbar~
        ImmersionBar.with(this)
                .transparentNavigationBar()
                .init();
        Activity activity = getActivity();
        int top = ImmersionBar.getNotchHeight(activity);
        int bottom = ImmersionBar.getNavigationBarHeight(activity);
        transferLayout.setPadding(0, top, 0, bottom);
    }

    public interface OnKeyBackListener {
        void onBack();
    }
}
