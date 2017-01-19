package com.hitomi.yifangbao.tilibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.yifangbao.tilibrary.anim.ITransferAnimator;
import com.hitomi.yifangbao.tilibrary.anim.Location;

import java.lang.reflect.Field;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferLayout extends FrameLayout {

    private Context context;
    private ViewPager viewPager;
    private ImageView smallImage;
    private ImageView originImage;

    private int displayIndex;
    private List<Integer> imageUrlList;

    private ITransferAnimator transferAnima;

    private OnViewPagerInstantiateListener onInstantiateListener = new OnViewPagerInstantiateListener() {
        @Override
        public void onInstantiate() {
            transferAnima();
        }
    };

    public TransferLayout(Context context) {
        this(context, null);
    }

    public TransferLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initLayout();
    }

    private void initLayout() {
        setBackgroundColor(Color.BLACK);
        initViewPager();
    }

    private void initViewPager() {
        viewPager = new ViewPager(context);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                LinearLayout parentLayout = new LinearLayout(context);
                LinearLayout.LayoutParams linlp = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
                parentLayout.setLayoutParams(linlp);

                smallImage = new ImageView(context);
                smallImage.setImageDrawable(originImage.getDrawable());

                LinearLayout.LayoutParams sImageVlp = new LinearLayout.LayoutParams(
                        originImage.getWidth(), originImage.getHeight());
                smallImage.setLayoutParams(sImageVlp);

                final int[] location = new int[2];
                originImage.getLocationInWindow(location);
                smallImage.setX(location[0]);
                smallImage.setY(location[1] - getStatusBarHeight());

                parentLayout.addView(smallImage);
                container.addView(parentLayout);
                onInstantiateListener.onInstantiate();
                parentLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                return parentLayout;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        LayoutParams vpLp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        viewPager.setLayoutParams(vpLp);
        viewPager.setCurrentItem(displayIndex);
        viewPager.setOffscreenPageLimit(2);
        addView(viewPager);
    }

    public void show() {
        addToWindow();
    }

    public void dismiss() {
        removeView();
    }

    private void removeView() {
        ViewGroup vg = (ViewGroup) this.getParent();
        if (vg != null) {
            vg.removeView(this);
        }
    }

    private void addToWindow() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Activity activity = (Activity) context;
        activity.getWindow().addContentView(this, windowLayoutParams);
    }

    private void transferAnima() {
        int[] location = new int[2];
        originImage.getLocationInWindow(location);
        Location oLocation =  new Location();
        oLocation.setX(location[0]);
        oLocation.setY(location[0]);
        oLocation.setWidth(originImage.getWidth());
        oLocation.setHeight(originImage.getHeight());
        transferAnima.showAnimator(smallImage, oLocation);
    }

    public void setOriginImage(ImageView originImage) {
        this.originImage = originImage;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public void setImageUrlList(List<Integer> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }

    private interface OnViewPagerInstantiateListener {
        void onInstantiate();
    }

    public static class Builder {
        private Context context;
        private int backgroundColor;

        private List<Bitmap> bitmapList;
        private List<String> imageStrList;
        private List<Integer> imageResLsit;

        private ITransferAnimator transferAnima;

        public TransferLayout create() {
            TransferLayout transferLayout = new TransferLayout(context);

            return transferLayout;
        }


    }


}
