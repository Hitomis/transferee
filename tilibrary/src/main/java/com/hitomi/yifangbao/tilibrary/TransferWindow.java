package com.hitomi.yifangbao.tilibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IIndexIndicator;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;
import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferWindow extends FrameLayout {

    private Context context;
    private TransferAttr attr;

    private ViewPager viewPager;
    private ImageView sharedImage;
    private ViewPager.OnPageChangeListener pageChangeListener;

    private ITransferAnimator transferAnimator;
    private TransferPagerAdapter imagePagerAdapter;
    private LinearLayout sharedLayout;

    private boolean shown;
    private Set<Integer> loadedIndexSet;

    private TransferWindow(Context context, TransferAttr attr) {
        super(context);
        this.context = context;
        this.attr = attr;
        loadedIndexSet = new HashSet<>();
        transferAnimator = attr.getTransferAnima();
        initLayout();
    }

    private void initLayout() {
        setBackgroundColor(attr.getBackgroundColor());
        initViewPager();
        initSharedLayout();
    }

    private void initSharedLayout() {
        ImageView currOriginImage = attr.getOriginImageList().get(attr.getCurrOriginIndex());
        LinearLayout.LayoutParams sImageVlp = new LinearLayout.LayoutParams(currOriginImage.getWidth(), currOriginImage.getHeight());

        final int[] location = new int[2];
        currOriginImage.getLocationInWindow(location);

        sharedImage = new ImageView(context);
        sharedImage.setImageDrawable(currOriginImage.getDrawable());
        sharedImage.setLayoutParams(sImageVlp);
        sharedImage.setX(location[0]);
        sharedImage.setY(location[1] - getStatusBarHeight());

        LinearLayout.LayoutParams linlp = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        sharedLayout = new LinearLayout(context);
        sharedLayout.setLayoutParams(linlp);
        sharedLayout.addView(sharedImage);

        addView(sharedLayout);
        showAnima();
    }

    private void initViewPager() {
        pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                attr.setCurrOriginIndex(position);
                attr.setCurrShowIndex(position);

                if (!loadedIndexSet.contains(position)) {
                    loadImage(position);
                    loadedIndexSet.add(position);
                }
                for (int i = 1; i <= attr.getOffscreenPageLimit(); i++) {
                    int left = position - i;
                    int right = position + i;
                    if (left >= 0 && !loadedIndexSet.contains(left)) {
                        loadImage(left);
                        loadedIndexSet.add(left);
                    }
                    if (right < attr.getImageStrList().size() && !loadedIndexSet.contains(right)) {
                        loadImage(right);
                        loadedIndexSet.add(right);
                    }
                }

            }
        };
        viewPager = new ViewPager(context);
        viewPager.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        viewPager.setVisibility(View.INVISIBLE);
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setOffscreenPageLimit(attr.getOffscreenPageLimit() + 2);
        addView(viewPager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewPager.removeOnPageChangeListener(pageChangeListener);
    }

    public void show() {
        if (!shown) {
            shown = true;
            addToWindow();
        }
    }

    public void dismiss() {
        if (!shown) return;
        shown = false;

        IProgressIndicator progressIndicator = attr.getProgressIndicator();
        if (progressIndicator != null)
            progressIndicator.hideView(attr.getCurrShowIndex());

        if (transferAnimator == null) {
            removeFromWindow();
        } else {
            if (attr.getCurrShowIndex() > attr.getCurrOriginIndex()) {
                dismissMissAnima();
            } else {
                dismissHitAnima();
            }
            dismissBackAnima();
        }
    }

    private void showAnima() {
        if (transferAnimator == null) return;
        Animator animator = transferAnimator.showAnimator(attr.getCurrOriginImageView(), sharedImage);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                imagePagerAdapter = new TransferPagerAdapter(attr.getImageStrList().size());
                imagePagerAdapter.setOnDismissListener(new TransferPagerAdapter.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        dismiss();
                    }
                });

                viewPager.setVisibility(View.VISIBLE);
                viewPager.setAdapter(imagePagerAdapter);
                viewPager.setCurrentItem(attr.getCurrOriginIndex());
                if (attr.getCurrOriginIndex() == 0)
                    pageChangeListener.onPageSelected(attr.getCurrOriginIndex());

                removeView(sharedLayout);

                initMainUI();
            }
        });
        animator.start();
    }

    private void initMainUI() {
        IIndexIndicator indexIndicator = attr.getIndexIndicator();
        if (indexIndicator != null && attr.getImageStrList().size() >= 2)
            indexIndicator.attach(this, viewPager);
    }

    private void dismissHitAnima() {
        final View beforeView = imagePagerAdapter.getImageItem(attr.getCurrShowIndex());
        final View afterView = attr.getCurrOriginImageView();
        afterView.setVisibility(View.INVISIBLE);

        Animator animator = transferAnimator.dismissHitAnimator(beforeView, afterView);
        if (animator == null) return;
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                removeFromWindow();
                afterView.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }

    private void dismissMissAnima() {
        View beforeView = imagePagerAdapter.getImageItem(attr.getCurrShowIndex());
        Animator animator = transferAnimator.dismissMissAnimator(beforeView);
        if (animator == null) return;
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                removeFromWindow();
            }
        });
        animator.start();
    }

    private void dismissBackAnima() {
        Animator animator = transferAnimator.dismissBackgroundAnimator(this, attr.getBackgroundColor());
        if (animator != null)
            animator.start();
    }

    private void removeFromWindow() {
        ViewGroup vg = (ViewGroup) getParent();
        if (vg != null) {
            vg.removeView(TransferWindow.this);
        }
        attr.getImageLoader().cancel();
    }

    private void addToWindow() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Activity activity = (Activity) context;
        activity.getWindow().addContentView(this, windowLayoutParams);
    }

    /**
     * ImageView 缩放到指定大小
     * @param imageView imageView 对象
     * @param w 宽
     * @param h 高
     * @return
     */
    private Drawable resizeImage(ImageView imageView, int w, int h) {
        Bitmap BitmapOrg = drawable2Bitmap(imageView.getDrawable());
        if (BitmapOrg == null) return null;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = newWidth * 2.f / width;
        float scaleHeight = newHeight * 2.f / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }

    /**
     * drawable转bitmap
     *
     * @param drawable drawable对象
     * @return bitmap
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public void loadImage(final int position) {
        String imgUrl = attr.getImageStrList().get(position);
        Drawable placeHolder = null;
        if (position < attr.getOriginImageList().size()) {
            ImageView imageView = attr.getOriginImageList().get(position);
            int intrinsicWidth = imageView.getDrawable().getIntrinsicWidth();
            int intrinsicHeight = imageView.getDrawable().getIntrinsicHeight();
            int reHeight = getWidth() * intrinsicHeight / intrinsicWidth;
            placeHolder = resizeImage(imageView, getWidth(), reHeight);
        }

        attr.getImageLoader().loadImage(imgUrl, imagePagerAdapter.getImageItem(position), placeHolder, new ImageLoader.Callback() {

            private IProgressIndicator progressIndicator = attr.getProgressIndicator();

            @Override
            public void onStart() {
                if (progressIndicator == null) return;
                progressIndicator.attach(position, imagePagerAdapter.getParentItem(position));
                progressIndicator.onStart(position);
            }

            @Override
            public void onProgress(int progress) {
                if (progressIndicator == null) return;
                progressIndicator.onProgress(position, progress);
            }

            @Override
            public void onFinish() {
                if (progressIndicator == null) return;
                progressIndicator.onFinish(position);
            }
        });
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

    public static class Builder {
        private Context context;
        private List<ImageView> originImageList;

        private int originIndex;
        private int offscreenPageLimit;
        private int backgroundColor;

        private List<String> imageStrList;

        private ITransferAnimator transferAnima;
        private IProgressIndicator proIndicat;
        private IIndexIndicator indexIndicator;
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

        public Builder setOffscreenPageLimit(int offscreenPageLimit) {
            this.offscreenPageLimit = offscreenPageLimit;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
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

        public Builder setIndexIndicator(IIndexIndicator indexIndicator) {
            this.indexIndicator = indexIndicator;
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
            attr.setImageStrList(imageStrList);
            attr.setCurrOriginIndex(originIndex);
            attr.setOffscreenPageLimit(offscreenPageLimit == 0 ? 1 : offscreenPageLimit);
            attr.setProgressIndicator(proIndicat);
            attr.setIndexIndicator(indexIndicator);
            attr.setTransferAnima(transferAnima);
            attr.setImageLoader(imageLoader);

            TransferWindow transferLayout = new TransferWindow(context, attr);
            return transferLayout;
        }

    }


}
