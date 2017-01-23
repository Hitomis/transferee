package com.hitomi.yifangbao.tilibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;
import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;

import java.lang.reflect.Field;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferWindow extends FrameLayout {

    private ViewPager viewPager;
    private ViewPager.OnPageChangeListener pageChangeListener;
    private ImageView sharedImage, originCurrImage;

    private Context context;
    private TransferAttr attr;

    private ITransferAnimator transferAnimator;
    private ImageLoader imageLoader;

    private TransferPagerAdapter imagePagerAdapter;
    private LinearLayout sharedLayout;

    private TransferWindow(Context context, TransferAttr attr) {
        super(context);
        this.context = context;
        this.attr = attr;
        transferAnimator = attr.getTransferAnima();
        imageLoader = attr.getImageLoader();
        initLayout();
    }

    private void initLayout() {
        setBackgroundColor(attr.getBackgroundColor());
        initViewPager();
        initSharedLayout();
    }

    private void initSharedLayout() {
        sharedLayout = new LinearLayout(context);
        LinearLayout.LayoutParams linlp = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        sharedLayout.setLayoutParams(linlp);

        originCurrImage = attr.getOriginImageList().get(attr.getOriginCurrIndex());
        sharedImage = new ImageView(context);
        sharedImage.setImageDrawable(originCurrImage.getDrawable());

        LinearLayout.LayoutParams sImageVlp = new LinearLayout.LayoutParams(originCurrImage.getWidth(), originCurrImage.getHeight());
        sharedImage.setLayoutParams(sImageVlp);

        final int[] location = new int[2];
        originCurrImage.getLocationInWindow(location);
        sharedImage.setX(location[0]);
        sharedImage.setY(location[1] - getStatusBarHeight());

        sharedLayout.addView(sharedImage);
        addView(sharedLayout);
        showAnima();
    }

    private void initViewPager() {
        new ViewPager.SimpleOnPageChangeListener();
        pageChangeListener = new ViewPager.OnPageChangeListener() {

            private int lastPosition = attr.getOriginCurrIndex();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (lastPosition != position) {
                    System.out.println("postion:" + position);
                    System.out.println("positionOffset:" + positionOffset);
                    System.out.println("positionOffsetPixels:" + positionOffsetPixels);
                    lastPosition = position;
                }

            }

            @Override
            public void onPageSelected(int position) {
                originCurrImage = attr.getOriginImageList().get(position);
//                showImageHD(position);
//                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 0) {
                    System.out.println(11111);
                }

            }
        };

        imagePagerAdapter = new TransferPagerAdapter(attr);
        imagePagerAdapter.setOnDismissListener(new TransferPagerAdapter.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });

        viewPager = new ViewPager(context);
        viewPager.setVisibility(View.INVISIBLE);
        viewPager.setAdapter(imagePagerAdapter);
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        viewPager.setCurrentItem(attr.getOriginCurrIndex());
        viewPager.setOffscreenPageLimit(2);
        addView(viewPager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewPager.removeOnPageChangeListener(pageChangeListener);
    }

    public void show() {
        addToWindow();
    }

    public void dismiss() {
        dismissAnima();
    }

    private void showAnima() {
        if (transferAnimator == null) return;
        Animator animator = transferAnimator.showAnimator(originCurrImage, sharedImage);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!attr.isLocalLoad()) {
                    viewPager.setVisibility(View.VISIBLE);
                    removeView(sharedLayout);
                    // 加载高清图
//                    showImageHD(attr.getOriginCurrIndex());
                }
            }
        });
    }

    private void dismissAnima() {
        if (transferAnimator == null) return;
        Animator animator = transferAnimator.dismissAnimator(imagePagerAdapter.getPrimaryItem(), originCurrImage);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundColor(Color.TRANSPARENT);
                originCurrImage.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup vg = (ViewGroup) getParent();
                if (vg != null) {
                    vg.removeView(TransferWindow.this);
                }
                originCurrImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addToWindow() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Activity activity = (Activity) context;
        activity.getWindow().addContentView(this, windowLayoutParams);
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

    private void showImageHD(int position) {
        if (imageLoader == null) return;

        Uri uri = Uri.parse(attr.getImageStrList().get(position));
        imageLoader.loadImage(uri, position, imagePagerAdapter);
    }

    public static class Builder {
        private Context context;
        private List<ImageView> originImageList;
        private int originIndex;

        private int backgroundColor;

        private List<Bitmap> bitmapList;
        private List<String> imageStrList;

        private ITransferAnimator transferAnima;
        private IProgressIndicator proIndicat;
        private ImageLoader imageLoader;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setOriginImageList(List<ImageView> originImageList) {
            this.originImageList = originImageList;
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

        public Builder setTransferAnima(ITransferAnimator transferAnima) {
            this.transferAnima = transferAnima;
            return this;
        }

        public Builder setProgressIndicator(IProgressIndicator proIndicat) {
            this.proIndicat = proIndicat;
            return this;
        }

        public Builder setImageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            return this;
        }

        public TransferWindow create() {
            TransferAttr attr = new TransferAttr();
            attr.setOriginImageList(originImageList);
            attr.setBackgroundColor(backgroundColor);
            attr.setBitmapList(bitmapList);
            attr.setImageStrList(imageStrList);
            attr.setOriginCurrIndex(originIndex);
            attr.setProgressIndicator(proIndicat);
            attr.setTransferAnima(transferAnima);
            attr.setImageLoader(imageLoader);

            TransferWindow transferLayout = new TransferWindow(context, attr);
            return transferLayout;
        }

    }


}
