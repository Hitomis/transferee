package com.hitomi.yifangbao.tilibrary.style;

import android.view.View;
import android.view.ViewGroup;


public interface IProgressIndicator {
    /**
     * DO NOT add indicator view into parent! Only called once per load.
     * */
    View getView(int position, ViewGroup parent);

    void hideView(int position);

    void onStart(int position);

    /**
     * @param progress in range of {@code [0, 100]}
     */
    void onProgress(int position, int progress);

    void onFinish(int position);

}
