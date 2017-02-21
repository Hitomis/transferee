package com.hitomi.tilibrary.style;

import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;

/**
 * Created by hitomi on 2017/2/4.
 */

public interface IIndexIndicator {

    /**
     * 在父容器上添加一个图片索引指示器 UI 组件
     *
     * @param parent TransferImage
     */
    void attach(FrameLayout parent);

    /**
     * 显示图片索引指示器 UI 组件
     *
     * @param viewPager TransferImage
     */
    void onShow(ViewPager viewPager);

    /**
     * 隐藏图片索引指示器 UI 组件
     */
    void onHide();

    /**
     * 移除图片索引指示器 UI 组件
     */
    void onRemove();
}
