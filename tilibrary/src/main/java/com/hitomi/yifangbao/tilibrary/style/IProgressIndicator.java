package com.hitomi.yifangbao.tilibrary.style;

import android.view.View;

import com.hitomi.yifangbao.tilibrary.TransferWindow;


public interface IProgressIndicator {
    /**
     * DO NOT add indicator view into parent! Only called once per load.
     * */
    View getView(TransferWindow parent);

    void onStart();

    /**
     * @param progress in range of {@code [0, 100]}
     */
    void onProgress(int progress);

    void onFinish();
}
