package com.hitomi.tilibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.tilibrary.loader.ImageLoader;
import com.hitomi.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.tilibrary.style.IIndexIndicator;
import com.hitomi.tilibrary.style.IProgressIndicator;
import com.hitomi.tilibrary.style.ITransferAnimator;
import com.hitomi.tilibrary.style.anim.TransitionAnimator;
import com.hitomi.tilibrary.style.index.IndexCircleIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

/**
 * Main workflow: <br/>
 * 1、点击缩略图展示缩略图到 TransferImage 过渡动画 <br/>
 * 2、显示下载高清图片进度 <br/>
 * 3、记载完成显示高清图片 <br/>
 * 4、高清图支持手势缩放 <br/>
 * 5、关闭 TransferImage 展示 TransferImage 到原缩略图的过渡动画 <br/>
 * Created by hitomi on 2017/1/19.
 */
public class TransferImage extends FrameLayout {

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

    private TransferImage(Context context) {
        super(context);
        this.context = context;
        loadedIndexSet = new HashSet<>();
    }

    public static TransferImage getDefault(Context context) {
        return new TransferImage(context);
    }

    private void initLayout() {
        transferAnimator = attr.getTransferAnima();
        setBackgroundColor(attr.getBackgroundColor());
        initViewPager();
        initSharedLayout();
    }

    /**
     * 初始化一个共享布局, 在 TransferImage 上 添加在与之前点击的缩略图相同的位置, 通过动画模拟出于过渡动画相似的效果
     */
    private void initSharedLayout() {
        ImageView currOriginImage = attr.getOriginImageList().get(attr.getCurrOriginIndex());
        LinearLayout.LayoutParams sImageVlp = new LinearLayout.LayoutParams(currOriginImage.getWidth(), currOriginImage.getHeight());

        final int[] location = new int[2];
        currOriginImage.getLocationInWindow(location);

        sharedImage = new ImageView(context);
        sharedImage.setImageDrawable(currOriginImage.getDrawable());
        sharedImage.setScaleType(FIT_CENTER);
        sharedImage.setLayoutParams(sImageVlp);
        sharedImage.setX(location[0]);
        sharedImage.setY(location[1] - getStatusBarHeight());

        LinearLayout.LayoutParams linlp = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        sharedLayout = new LinearLayout(context);
        sharedLayout.setLayoutParams(linlp);
        sharedLayout.addView(sharedImage);

        addView(sharedLayout);
        startShowing();
    }

    /**
     * 初始化 ViewPager
     */
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
        viewPager.setOffscreenPageLimit(attr.getImageStrList().size() + 1);
        addView(viewPager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // unregister PageChangeListener
        viewPager.removeOnPageChangeListener(pageChangeListener);
    }

    /**
     * TransferImage 是否显示
     *
     * @return true ：显示, false ：关闭
     */
    public boolean isShown() {
        return shown;
    }

    /**
     * 显示 TransferImage
     */
    public void show() {
        if (!shown) {
            shown = true;
            addToWindow();
            initLayout();
        }
    }

    /**
     * 关闭 TransferImage
     */
    public void dismiss() {
        if (!shown) return;
        shown = false;

        IProgressIndicator progressIndicator = attr.getProgressIndicator();
        if (progressIndicator != null)
            progressIndicator.hideView(attr.getCurrShowIndex());

        if (transferAnimator == null) {
            removeFromWindow();
        } else {
            startDismissing();
        }
    }

    /**
     * 初始化 TransferImage 主面板
     */
    private void initMainPanel() {
        addIndexIndicator();
    }

    /**
     * 开启显示 TransferImage 动画
     */
    private void startShowing() {
        if (transferAnimator == null && !shown) return;
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

                initMainPanel();
            }
        });
        animator.start();
    }

    /**
     * 在 TransferImage 面板中添加下标指示器 UI 组件
     */
    private void addIndexIndicator() {
        IIndexIndicator indexIndicator = attr.getIndexIndicator();
        if (indexIndicator != null && attr.getImageStrList().size() >= 2) {
            indexIndicator.attach(this);
            indexIndicator.onShow(viewPager);
        }
    }

    /**
     * 从 TransferImage 面板中移除下标指示器 UI 组件
     */
    private void removeIndexIndicator() {
        IIndexIndicator indexIndicator = attr.getIndexIndicator();
        if (indexIndicator != null && attr.getImageStrList().size() >= 2) {
            indexIndicator.onRemove();
        }
    }

    /**
     * 开启关闭动画
     */
    private void startDismissing() {
        Animator dismissAnimator;
        if (attr.getCurrShowIndex() > attr.getCurrOriginIndex()) {
            dismissAnimator = getDismissMissAnimator();
        } else {
            dismissAnimator = getDismissHitAnimator();
        }
        Animator dismissBackgroundAnimator = getDismissBackgroundAnimator();

        Animator animator;
        if (dismissBackgroundAnimator != null) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(dismissAnimator).with(dismissBackgroundAnimator);
            animator = animatorSet;
        } else {
            animator = dismissAnimator;
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeIndexIndicator();
                loadedIndexSet.clear();
                removeView(viewPager);
                removeFromWindow();
            }
        });

        animator.start();
    }

    /**
     * 获取 TransferImage 与之前缩略图对应的关闭动画
     */
    private Animator getDismissHitAnimator() {
        final View beforeView = imagePagerAdapter.getImageItem(attr.getCurrShowIndex());
        final View afterView = attr.getCurrOriginImageView();
        afterView.setVisibility(View.INVISIBLE);

        Animator animator = transferAnimator.dismissHitAnimator(beforeView, afterView);

        if (animator != null)
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    afterView.setVisibility(View.VISIBLE);
                }
            });

        return animator;
    }

    /**
     * 获取 TransferImage 未与之前缩略图对应的关闭动画
     */
    private Animator getDismissMissAnimator() {
        View beforeView = imagePagerAdapter.getImageItem(attr.getCurrShowIndex());
        Animator animator = transferAnimator.dismissMissAnimator(beforeView);
        return animator;
    }

    /**
     * 获取 TransferImage 背景关闭动画
     */
    private Animator getDismissBackgroundAnimator() {
        return transferAnimator.dismissBackgroundAnimator(this, attr.getBackgroundColor());
    }

    /**
     * 从 Window 中移除 TransferImage
     */
    private void removeFromWindow() {
        ViewGroup vg = (ViewGroup) getParent();
        if (vg != null) {
            vg.removeView(TransferImage.this);
        }
        attr.getImageLoader().cancel();
    }

    /**
     * 将 TransferImage 添加到 Window 中
     */
    private void addToWindow() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Activity activity = (Activity) context;
        activity.getWindow().addContentView(this, windowLayoutParams);
    }

    /**
     * ImageView 缩放到指定大小
     *
     * @param imageView imageView 对象
     * @param w         宽
     * @param h         高
     * @return Drawable
     */
    private Drawable resizeImage(ImageView imageView, int w, int h) {
        Bitmap BitmapOrg = drawable2Bitmap(imageView.getDrawable());
        if (BitmapOrg == null) return null;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        float scaleWidth = w * 2.f / width;
        float scaleHeight = h * 2.f / height;

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

    /**
     * 加载高清图
     *
     * @param position 图片所在索引位置下标
     */
    private void loadImage(final int position) {
        String imgUrl = attr.getImageStrList().get(position);
        Drawable placeHolder = getPlaceHolderDrawable(position);

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
     * 获取加载完高清图之前的占位图 Drawable
     *
     * @param position 图片索引
     * @return 占位图 Drawable
     */
    @Nullable
    private Drawable getPlaceHolderDrawable(int position) {
        Drawable placeHolder = null;
        if (position < attr.getOriginImageList().size()) {
            ImageView imageView = attr.getOriginImageList().get(position);
//            int intrinsicWidth = imageView.getDrawable().getIntrinsicWidth();
//            int intrinsicHeight = imageView.getDrawable().getIntrinsicHeight();
//            int reHeight = getWidth() * intrinsicHeight / intrinsicWidth;
            placeHolder = imageView.getDrawable();
        } else {
            if (attr.getMissPlaceHolder() != 0)
                placeHolder = context.getResources().getDrawable(attr.getMissPlaceHolder());
        }
        return placeHolder;
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度值 unit ：px
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

    private void applyAttr(TransferAttr attr) {
        this.attr = attr;
    }

    public static class Builder {
        private Context context;
        private ImageView[] originImages;
        private List<ImageView> originImageList;

        private int originIndex;
        private int offscreenPageLimit;
        private int backgroundColor;
        private int missPlaceHolder;

        private String[] imageUrls;
        private List<String> imageUrlList;

        private ITransferAnimator transferAnima;
        private IProgressIndicator progressIndicat;
        private IIndexIndicator indexIndicator;
        private ImageLoader imageLoader;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setOriginImages(ImageView... originImages) {
            this.originImages = originImages;
            return this;
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

        public Builder setMissPlaceHolder(int missPlaceHolder) {
            this.missPlaceHolder = missPlaceHolder;
            return this;
        }

        public Builder setImageUrls(String... imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public Builder setImageUrlList(List<String> imageUrlList) {
            this.imageUrlList = imageUrlList;
            return this;
        }

        public Builder setTransferAnima(ITransferAnimator transferAnima) {
            this.transferAnima = transferAnima;
            return this;
        }

        public Builder setProgressIndicator(IProgressIndicator proIndicat) {
            this.progressIndicat = proIndicat;
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

        public TransferImage setup(TransferImage transferImage) {
            if (transferImage.isShown()) return transferImage;

            TransferAttr attr = new TransferAttr();

            if (originImageList != null && !originImageList.isEmpty()) {
                attr.setOriginImageList(originImageList);
            } else {
                attr.setOriginImageList(Arrays.asList(originImages));
            }

            if (imageUrlList != null && !imageUrlList.isEmpty()) {
                attr.setImageUrlList(imageUrlList);
            } else {
                attr.setImageUrlList(Arrays.asList(imageUrls));
            }

            if (progressIndicat == null) {
                attr.setProgressIndicator(new ProgressPieIndicator());
            } else {
                attr.setProgressIndicator(progressIndicat);
            }

            if (indexIndicator == null) {
                attr.setIndexIndicator(new IndexCircleIndicator());
            } else {
                attr.setIndexIndicator(indexIndicator);
            }

            if (transferAnima == null) {
                attr.setTransferAnima(new TransitionAnimator());
            } else {
                attr.setTransferAnima(transferAnima);
            }

            if (imageLoader == null) {
                // Fix splash screen bug : context replace applicationContext
                attr.setImageLoader(GlideImageLoader.with(context.getApplicationContext()));
            } else {
                attr.setImageLoader(imageLoader);
            }

            attr.setOffscreenPageLimit(offscreenPageLimit <= 0 ? 1 : offscreenPageLimit);
            attr.setBackgroundColor(backgroundColor == 0 ? Color.BLACK : backgroundColor);
            attr.setCurrOriginIndex(originIndex < 0 ? 0 : originIndex);
            attr.setMissPlaceHolder(missPlaceHolder);

            transferImage.applyAttr(attr);
            return transferImage;
        }

    }
}
