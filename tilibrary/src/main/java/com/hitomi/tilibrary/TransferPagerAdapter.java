package com.hitomi.tilibrary;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.view.photoview.PhotoView;

import java.util.Map;
import java.util.WeakHashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 展示高清图的图片数据适配器
 * Created by hitomi on 2017/1/23.
 */
public class TransferPagerAdapter extends PagerAdapter {
    @IdRes
    private static final int ID_IMAGE = 1001;

    private int size;
    private Map<Integer, FrameLayout> containnerLayoutMap;
    private OnDismissListener onDismissListener;

    public TransferPagerAdapter(int imageSize) {
        size = imageSize;
        containnerLayoutMap = new WeakHashMap<>();
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * 获取指定索引页面中的 ImageView
     *
     * @param position
     * @return
     */
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
            parentLayout = newParentLayout(container.getContext());
            containnerLayoutMap.put(position, parentLayout);
        }
        container.addView(parentLayout);
        return parentLayout;
    }

    @NonNull
    private FrameLayout newParentLayout(Context context) {
        // create inner ImageView
        final PhotoView imageView = new PhotoView(context);
        imageView.setId(ID_IMAGE);
        imageView.enable();
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageLp.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageLp);

        // create outer ParentLayout
        FrameLayout parentLayout = new FrameLayout(context);
        FrameLayout.LayoutParams parentLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        parentLayout.setLayoutParams(parentLp);

        parentLayout.addView(imageView);

        // add listener to parentLayout
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.reset();
                onDismissListener.onDismiss();
            }
        });
        return parentLayout;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

}
