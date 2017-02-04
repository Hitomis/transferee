package com.hitomi.yifangbao.tilibrary.style.index;

import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.hitomi.yifangbao.tilibrary.style.IIndexIndicator;
import com.hitomi.yifangbao.tilibrary.style.view.CircleIndicator;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by hitomi on 2017/2/4.
 */

public class IndexCircleIndicator implements IIndexIndicator {

    @Override
    public void attach(FrameLayout parent, ViewPager viewPager) {
        FrameLayout.LayoutParams indexLp = new FrameLayout.LayoutParams(WRAP_CONTENT, 48);
        indexLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        CircleIndicator circleIndicator = new CircleIndicator(parent.getContext());
        circleIndicator.setViewPager(viewPager);
        circleIndicator.setGravity(Gravity.CENTER_VERTICAL);
        circleIndicator.setLayoutParams(indexLp);

        parent.addView(circleIndicator);
    }
}
