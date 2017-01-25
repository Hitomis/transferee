package com.hitomi.yifangbao.tilibrary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.photoview.PhotoView;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;

import java.util.HashMap;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/23.
 */

public class TransferPagerAdapter extends PagerAdapter {
    @IdRes
    private static final int ID_IMAGE = 1001;

    private TransferAttr attr;
    private Map<Integer, FrameLayout> containnerLayoutMap;
    private IProgressIndicator progressIndicator;
    private OnDismissListener onDismissListener;

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

    public PhotoView getImageItem(int position) {
        FrameLayout parentLayout = containnerLayoutMap.get(position);
        int childCount = parentLayout.getChildCount();
        PhotoView imageView = null;
        for (int i = 0; i < childCount; i++) {
            View view = parentLayout.getChildAt(i);
            if (view instanceof PhotoView) {
                imageView = (PhotoView) view;
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
            loadImageHD((PhotoView) parentLayout.findViewById(ID_IMAGE), position);
            containnerLayoutMap.put(position, parentLayout);
        }
        container.addView(parentLayout);

        if (attr.getCurrOriginIndex() == position) {
            // init value currShowIndex
            attr.setCurrShowIndex(position);
        }
        return parentLayout;
    }

    @NonNull
    private FrameLayout newParentLayout(Context context, int position) {
        // create inner ImageView
        PhotoView imageView = new PhotoView(context);
        imageView.setId(ID_IMAGE);
        imageView.enable();
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
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
                onDismissListener.onDismiss();
            }
        });
        return parentLayout;
    }


    private void loadImageHD(ImageView imageView, final int position) {
        String imgUrl = attr.getImageStrList().get(position);
        Drawable placeHolder = null;
        if (position < attr.getOriginImageList().size()) {
            placeHolder = attr.getOriginImageList().get(position).getDrawable();
        }

        attr.getImageLoader().loadImage(imgUrl, imageView, placeHolder, new ImageLoader.Callback() {

            @Override
            public void onStart() {
                if (progressIndicator == null) return;
                progressIndicator.getView(position, getParentItem(position));
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


    public interface OnDismissListener {
        void onDismiss();
    }

}
