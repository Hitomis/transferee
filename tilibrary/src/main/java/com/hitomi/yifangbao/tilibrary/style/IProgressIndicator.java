package com.hitomi.yifangbao.tilibrary.style;

import android.view.View;
import android.view.ViewGroup;


public interface IProgressIndicator {
    /**
     * DO NOT add indicator view into parent! Only called once per load.
     * */
    View getView(ViewGroup parent);

    void onStart();

    /**
     * @param progress in range of {@code [0, 100]}
     */
    void onProgress(int progress);

    void onFinish();
}
