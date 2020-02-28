package com.hitomi.tilibrary.style;

import androidx.viewpager.widget.ViewPager;
import android.widget.FrameLayout;

/**
 * 图片索引指示器接口，实现 IIndexIndicator 可扩展自己的图片指示器组件
 * <p>
 * email: 196425254@qq.com
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
