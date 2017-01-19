package com.hitomi.yifangbao.tilibrary.anim;

import android.view.View;

/**
 * Created by hitomi on 2017/1/19.
 */

public interface ITransferAnimator {

    void showAnimator(View sharedView, Location originViewLocation);

    void dismissAnimator(View sharedView);

}
