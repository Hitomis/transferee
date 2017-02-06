package com.hitomi.yifangbao.tilibrary.style;

import android.view.View;
import android.widget.FrameLayout;


public interface IProgressIndicator {

    View attach(int position, FrameLayout parent);

    void hideView(int position);

    void onStart(int position);

    void onProgress(int position, int progress);

    void onFinish(int position);

}
