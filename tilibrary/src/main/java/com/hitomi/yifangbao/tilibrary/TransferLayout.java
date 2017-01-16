package com.hitomi.yifangbao.tilibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;
import com.hitomi.yifangbao.tilibrary.style.Location;

import java.lang.reflect.Field;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferLayout extends FrameLayout {

    private Context context;
    private TransferAttr attr;

    private ViewPager viewPager;
    private ImageView sharedImage;

    private OnViewPagerInstantiateListener onInstantiateListener = new OnViewPagerInstantiateListener() {
        @Override
        public void onInstantiate() {
            transferAnima();
        }
    };

    private TransferLayout(Context context, TransferAttr attr) {
        super(context);
        this.context = context;
        this.attr = attr;
        initLayout();
    }

    private void initLayout() {
        setBackgroundColor(attr.getBackgroundColor());
        initViewPager();
    }

    private void initViewPager() {
        viewPager = new ViewPager(context);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return attr.getImageSize();
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

                sharedImage = new ImageView(context);
                sharedImage.setImageDrawable(attr.getOriginImage().getDrawable());

                LinearLayout.LayoutParams sImageVlp = new LinearLayout.LayoutParams(
                        attr.getOriginImage().getWidth(), attr.getOriginImage().getHeight());
                sharedImage.setLayoutParams(sImageVlp);

                final int[] location = new int[2];
                attr.getOriginImage().getLocationInWindow(location);
                sharedImage.setX(location[0]);
                sharedImage.setY(location[1] - getStatusBarHeight());

                parentLayout.addView(sharedImage);
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
        viewPager.setCurrentItem(attr.getOriginIndex());
        viewPager.setOffscreenPageLimit(2);
        addView(viewPager);
    }

    public void show() {
        addToWindow();
    }

    public void dismiss() {
        attr.getTransferAnima().dismissAnimator(this);
    }

    private void addToWindow() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Activity activity = (Activity) context;
        activity.getWindow().addContentView(this, windowLayoutParams);
    }

    private void transferAnima() {
        attr.getTransferAnima().showAnimator(this);
    }

    public View getSharedView() {
        return sharedImage;
    }

    public View getOriginView() {
        return attr.getOriginImage();
    }

    public Location getOriginLocation() {
        int[] location = new int[2];
        attr.getOriginImage().getLocationInWindow(location);
        Location oLocation =  new Location();
        oLocation.setX(location[0]);
        oLocation.setY(location[1]);
        oLocation.setWidth(attr.getOriginImage().getWidth());
        oLocation.setHeight(attr.getOriginImage().getHeight());
        return oLocation;
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

    private void setAttribute(TransferAttr attribute) {
        attr = attribute;
    }

    private interface OnViewPagerInstantiateListener {
        void onInstantiate();
    }



    public static class Builder {
        private Context context;
        private ImageView originImage;
        private int originIndex;

        private int backgroundColor;

        private List<Bitmap> bitmapList;
        private List<String> imageStrList;
        private List<Integer> imageResLsit;

        private ITransferAnimator transferAnima;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setOriginImage(ImageView originImage) {
            this.originImage = originImage;
            return this;
        }

        public Builder setOriginIndex(int originIndex) {
            this.originIndex = originIndex;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setBitmapList(List<Bitmap> bitmapList) {
            this.bitmapList = bitmapList;
            return this;
        }

        public Builder setImageStrList(List<String> imageStrList) {
            this.imageStrList = imageStrList;
            return this;
        }

        public Builder setImageResLsit(List<Integer> imageResLsit) {
            this.imageResLsit = imageResLsit;
            return this;
        }

        public Builder setTransferAnima(ITransferAnimator transferAnima) {
            this.transferAnima = transferAnima;
            return this;
        }

        public TransferLayout create() {
            TransferAttr attr = new TransferAttr();
            attr.setOriginImage(originImage);
            attr.setBackgroundColor(backgroundColor);
            attr.setBitmapList(bitmapList);
            attr.setImageStrList(imageStrList);
            attr.setImageResList(imageResLsit);
            attr.setTransferAnima(transferAnima);
            attr.setOriginIndex(originIndex);

            TransferLayout transferLayout = new TransferLayout(context, attr);
            return transferLayout;
        }


    }


}
