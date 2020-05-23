package com.hitomi.tilibrary.style.index;

import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.view.indicator.CircleIndicator;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * 图片翻页时使用 {@link CircleIndicator} 去指示当前图片的位置
 * <p>
 * Created by Vans Z on 2017/2/4.
 * <p>
 * email: 196425254@qq.com
 */
public class CircleIndexIndicator implements IIndexIndicator {

    private CircleIndicator circleIndicator;

    @Override
    public void attach(FrameLayout parent) {
        FrameLayout.LayoutParams indexLp = new FrameLayout.LayoutParams(WRAP_CONTENT, 48);
        indexLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        indexLp.bottomMargin = 10;

        circleIndicator = new CircleIndicator(parent.getContext());
        circleIndicator.setGravity(Gravity.CENTER_VERTICAL);
        circleIndicator.setLayoutParams(indexLp);

        parent.addView(circleIndicator);
    }

    @Override
    public void onShow(ViewPager viewPager) {
        if (circleIndicator == null) return;
        circleIndicator.setVisibility(View.VISIBLE);
        circleIndicator.setViewPager(viewPager);
    }

    @Override
    public void onHide() {
        if (circleIndicator == null) return;
        circleIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onRemove() {
        if (circleIndicator == null) return;
        ViewGroup vg = (ViewGroup) circleIndicator.getParent();
        if (vg != null) {
            vg.removeView(circleIndicator);
        }
    }
}
