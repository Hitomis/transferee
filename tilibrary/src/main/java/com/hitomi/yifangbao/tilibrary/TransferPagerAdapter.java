package com.hitomi.yifangbao.tilibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2017/1/23.
 */

public class TransferPagerAdapter extends PagerAdapter {

    private TransferAttr attr;
    private ImageView currImageView;
    private OnDismissListener onDismissListener;

    public TransferPagerAdapter(TransferAttr attr) {
        this.attr = attr;
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
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();
        FrameLayout parentLayout = new FrameLayout(context);
        FrameLayout.LayoutParams parentLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        parentLayout.setLayoutParams(parentLp);


        ImageView imageView = new ImageView(context);
        ImageView originImage = attr.getOriginImageList().get(position);
        imageView.setImageDrawable(originImage.getDrawable());

        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageView.setLayoutParams(imageLp);

        parentLayout.addView(imageView);
        container.addView(parentLayout);

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
                        if (diffX >= 36 || diffY >= 36) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
        if (attr.getOriginCurrIndex() == position) {
            setPrimaryItem(container, position, imageView);
        }
        return parentLayout;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (object instanceof ImageView)
            currImageView = (ImageView) object;
    }

    public ImageView getPrimaryItem() {
        return currImageView;
    }

    public ViewGroup getPrimaryItemParentLayout() {
        return (ViewGroup) getPrimaryItem().getParent();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

}
