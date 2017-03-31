package com.hitomi.tilibrary.transfer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hitomi.tilibrary.view.image.TransferImage;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * 展示高清图的图片数据适配器
 * Created by hitomi on 2017/1/23.
 */
class TransferAdapter extends PagerAdapter {

    private int showIndex;
    private int imageSize;

    private OnInstantiateItemListener onInstantListener;

    private SparseArray<FrameLayout> containLayoutArray;

    TransferAdapter(int imageSize, int nowThumbnailIndex) {
        this.imageSize = imageSize;
        this.showIndex = nowThumbnailIndex + 1 == imageSize
                ? nowThumbnailIndex - 1 : nowThumbnailIndex + 1;
        this.showIndex = showIndex < 0 ? 0 : showIndex;

        containLayoutArray = new SparseArray<>();
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
    TransferImage getImageItem(int position) {
        TransferImage transImage = null;

        FrameLayout parentLayout = containLayoutArray.get(position);
        if (parentLayout != null) {
            int childCount = parentLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = parentLayout.getChildAt(i);
                if (view instanceof ImageView) {
                    transImage = (TransferImage) view;
                    break;
                }
            }
        }

        return transImage;
    }

    FrameLayout getParentItem(int position) {
        return containLayoutArray.get(position);
    }

    void setOnInstantListener(OnInstantiateItemListener listener) {
        this.onInstantListener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // ViewPager instantiateItem 顺序：从 position 开始递减到0位置，再从 positon 递增到 getCount() - 1
        FrameLayout parentLayout = containLayoutArray.get(position);
        if (parentLayout == null) {
            parentLayout = newParentLayout(container, position);
            containLayoutArray.put(position, parentLayout);
        }
        container.addView(parentLayout);
        if (position == showIndex && onInstantListener != null)
            onInstantListener.onComplete();
        return parentLayout;
    }

    @NonNull
    private FrameLayout newParentLayout(ViewGroup container, final int pos) {
        Context context = container.getContext();
        // create inner ImageView
        TransferImage imageView = new TransferImage(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        // create outer ParentLayout
        FrameLayout parentLayout = new FrameLayout(context);
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        parentLayout.addView(imageView);

        return parentLayout;
    }

    interface OnInstantiateItemListener {
        void onComplete();
    }

}
