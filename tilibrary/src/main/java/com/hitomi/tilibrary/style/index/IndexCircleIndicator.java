package com.hitomi.tilibrary.style.index;

import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.view.CircleIndicator;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by hitomi on 2017/2/4.
 */

public class IndexCircleIndicator implements IIndexIndicator {

    private CircleIndicator circleIndicator;

    @Override
    public void attach(FrameLayout parent) {
        FrameLayout.LayoutParams indexLp = new FrameLayout.LayoutParams(WRAP_CONTENT, 48);
        indexLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        circleIndicator = new CircleIndicator(parent.getContext());
        circleIndicator.setGravity(Gravity.CENTER_VERTICAL);
        circleIndicator.setLayoutParams(indexLp);

        parent.addView(circleIndicator);
    }

    @Override
    public void onShow(ViewPager viewPager) {
        circleIndicator.setVisibility(View.VISIBLE);
        circleIndicator.setViewPager(viewPager);
    }

    @Override
    public void onHide() {
        circleIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onRemove() {
        ViewGroup vg = (ViewGroup) circleIndicator.getParent();

        if (vg != null) {
            vg.removeView(circleIndicator);
        }
    }
}
