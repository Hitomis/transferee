package com.hitomi.yifangbao.tilibrary;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.view.PagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/23.
 */

public class TransferPagerAdapter extends PagerAdapter implements ImageLoader.Callback {

    private static final int TOUCH_SLOP = 36;

    private TransferAttr attr;
    private Map<Integer, FrameLayout> containnerLayoutMap;
    private IProgressIndicator progressIndicator;
    private OnDismissListener onDismissListener;

    private Handler handler = new Handler();

    public TransferPagerAdapter(TransferAttr attr) {
        this.attr = attr;
        containnerLayoutMap = new HashMap<>();
        progressIndicator = attr.getProgressIndicator();
    }

    @Override
    public int getCount() {
        return attr.getImageSize();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public ImageView getImageItem(int position) {
        FrameLayout parentLayout = containnerLayoutMap.get(position);
        int childCount = parentLayout.getChildCount();
        ImageView imageView = null;
        for (int i = 0; i < childCount; i++) {
            View view = parentLayout.getChildAt(i);
            if (view instanceof ImageView) {
                imageView = (ImageView) view;
                break;
            }
        }
        return imageView;
    }

    public FrameLayout getParentItem(int position) {
        return containnerLayoutMap.get(position);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout parentLayout = containnerLayoutMap.get(position);
        if (parentLayout == null) {
            parentLayout = newParentLayout(container.getContext(), position);
            containnerLayoutMap.put(position, parentLayout);
        }
        container.addView(parentLayout);

        if (attr.getCurrOriginIndex() == position) {
            // init value currShowIndex
            attr.setCurrShowIndex(position);
        }
        loadImageHD(position);
        return parentLayout;
    }

    @NonNull
    private FrameLayout newParentLayout(Context context, int position) {
        // create inner ImageView
        ImageView imageView = new ImageView(context);
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageView.setLayoutParams(imageLp);
        if (position < attr.getOriginImageList().size()) {
            ImageView originImage = attr.getOriginImageList().get(position);
            imageView.setImageDrawable(originImage.getDrawable());
        }

        // create outer ParentLayout
        FrameLayout parentLayout = new FrameLayout(context);
        FrameLayout.LayoutParams parentLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        parentLayout.setLayoutParams(parentLp);

        parentLayout.addView(imageView);

        // add listener to parentLayout
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDismissListener.onDismiss();
            }
        });
        parentLayout.setOnTouchListener(new View.OnTouchListener() {

            private float preX, preY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preX = event.getX();
                        preY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float diffX = Math.abs(event.getX() - preX);
                        float diffY = Math.abs(event.getY() - preY);
                        if (diffX >= TOUCH_SLOP || diffY >= TOUCH_SLOP) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
        return parentLayout;
    }


    private void loadImageHD(int position) {
        Uri uri = Uri.parse(attr.getImageStrList().get(position));
        attr.getImageLoader().downloadImage(uri, position, this);
    }

    @UiThread
    private void doShowImage(int position, File image) {
        ImageView imageView = getImageItem(position);
        attr.getImageLoader().loadImage(image, imageView);
    }

    @UiThread
    @Override
    public void onCacheHit(int position, File image) {
        doShowImage(position, image);
    }

    @WorkerThread
    @Override
    public void onCacheMiss(final int position, final File image) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doShowImage(position, image);
            }
        });
    }

    @WorkerThread
    @Override
    public void onStart(final int position) {
        if (progressIndicator == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressIndicator.getView(position, getParentItem(position));
            }
        });
    }

    @WorkerThread
    @Override
    public void onProgress(final int position, final int progress) {
        if (progressIndicator == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressIndicator.onProgress(position, progress);
            }
        });
    }

    @WorkerThread
    @Override
    public void onFinish(final int position) {
        if (progressIndicator == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressIndicator.onFinish(position);
            }
        });
    }

    public interface OnDismissListener {
        void onDismiss();
    }

}
