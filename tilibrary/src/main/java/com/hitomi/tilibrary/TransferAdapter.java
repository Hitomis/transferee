package com.hitomi.tilibrary;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.view.image.TransferImage;

import java.util.Map;
import java.util.WeakHashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 展示高清图的图片数据适配器
 * Created by hitomi on 2017/1/23.
 */
class TransferAdapter extends PagerAdapter {

    private int showIndex;
    private int imageSize;
    private TransferConfig config;

    private OnDismissListener onDismissListener;
    private OnInstantiateItemListener onInstantListener;

    private Map<Integer, FrameLayout> containnerLayoutMap;

    public TransferAdapter(TransferConfig config) {
        this.imageSize = config.getSourceImageList().size();
        this.showIndex = config.getNowThumbnailIndex() + 1 == imageSize
                ? config.getNowThumbnailIndex() - 1 : config.getNowThumbnailIndex() + 1;
        this.config = config;
        containnerLayoutMap = new WeakHashMap<>();
    }

    @Override
    public int getCount() {
        return imageSize;
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
     * 获取指定索引页面中的 TransferImage
     *
     * @param position
     * @return
     */
    public TransferImage getImageItem(int position) {
        FrameLayout parentLayout = containnerLayoutMap.get(position);
        int childCount = parentLayout.getChildCount();
        TransferImage transImage = null;
        for (int i = 0; i < childCount; i++) {
            View view = parentLayout.getChildAt(i);
            if (view instanceof ImageView) {
                transImage = (TransferImage) view;
                break;
            }
        }
        return transImage;
    }

    public FrameLayout getParentItem(int position) {
        return containnerLayoutMap.get(position);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public void setOnInstantListener(OnInstantiateItemListener listener) {
        this.onInstantListener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // ViewPager instantiateItem 顺序：从 position 开始递减到0位置，再从 positon 递增到 getCount() - 1
        FrameLayout parentLayout = containnerLayoutMap.get(position);
        if (parentLayout == null) {
            parentLayout = newParentLayout(container, position);
            containnerLayoutMap.put(position, parentLayout);
        }
        container.addView(parentLayout);
        if (position == showIndex && onInstantListener != null)
            onInstantListener.onComplete();
        return parentLayout;
    }

    @NonNull
    private FrameLayout newParentLayout(ViewGroup container, final int pos) {
        // create inner ImageView
        TransferImage imageView = new TransferImage(container.getContext());
        if (config.isThumbnailEmpty()) {
            Drawable originDrawable = config.getOriginImageList()
                    .get(pos).getDrawable();
            int locationX = (container.getMeasuredWidth() - originDrawable.getIntrinsicWidth()) / 2;
            int locationY = (container.getMeasuredHeight() - originDrawable.getIntrinsicHeight()) / 2;
            imageView.setOriginalInfo(locationX, locationY,
                    originDrawable.getIntrinsicWidth(), originDrawable.getIntrinsicHeight());
            imageView.transClip();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // create outer ParentLayout
        FrameLayout parentLayout = new FrameLayout(container.getContext());
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        parentLayout.addView(imageView);

        // add listener to parentLayout
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDismissListener.onDismiss(pos);
            }
        });
        return parentLayout;
    }

    interface OnDismissListener {
        void onDismiss(int pos);
    }

    interface OnInstantiateItemListener {
        void onComplete();
    }

}
