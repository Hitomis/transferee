package com.hitomi.tilibrary.style;

import android.animation.Animator;
import android.view.View;

/**
 * Created by hitomi on 2017/1/19.
 */

public interface ITransferAnimator {

    /**
     * 点击缩略图时创建一个开启并展现 TransferImage 的动画
     *
     * @param beforeView 缩略图对象
     * @param afterView  在 TransferImage 中模拟的缩略图对象
     * @return 显示 TransferImage 的属性动画
     */
    Animator showAnimator(View beforeView, View afterView);

    /**
     * 创建点击关闭 TransferImage 与之前缩略图可对应的动画
     *
     * @param beforeView TransferImage 中当前所显示的图片对象
     * @param afterView  缩略图对象
     * @return TransferImage 与之前缩略图对应的关闭动画
     */
    Animator dismissHitAnimator(View beforeView, View afterView);

    /**
     * 创建点击关闭 TransferImage 与之前缩略图不能对应的动画
     *
     * @param beforeView TransferImage 中当前所显示的图片对象
     * @return TransferImage 不能与之前缩略图对应的关闭动画
     */
    Animator dismissMissAnimator(View beforeView);

    /**
     * 创建 TransferImage 背景关闭动画
     *
     * @param parent          TransferImage
     * @param backgroundColor 背景色
     * @return TransferImage 背景关闭动画
     */
    Animator dismissBackgroundAnimator(View parent, int backgroundColor);

}
