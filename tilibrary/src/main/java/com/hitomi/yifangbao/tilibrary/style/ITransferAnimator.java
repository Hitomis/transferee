package com.hitomi.yifangbao.tilibrary.style;

import android.animation.Animator;
import android.view.View;

/**
 * Created by hitomi on 2017/1/19.
 */

public interface ITransferAnimator {

    Animator showAnimator(View beforeView, View afterView);

    Animator dismissAnimator(View beforeView, View afterView);

}
