package com.hitomi.tilibrary.style;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by hitomi on 2017/1/19.
 */
public class Location {

    private int x;

    private int y;

    private int realX;

    private int realY;

    private int width;

    private int height;

    private int realWidth;

    private int realHeight;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRealX() {
        return realX;
    }

    public void setRealX(int realX) {
        this.realX = realX;
    }

    public int getRealY() {
        return realY;
    }

    public void setRealY(int realY) {
        this.realY = realY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRealWidth() {
        return realWidth;
    }

    public void setRealWidth(int realWidth) {
        this.realWidth = realWidth;
    }

    public int getRealHeight() {
        return realHeight;
    }

    public void setRealHeight(int realHeight) {
        this.realHeight = realHeight;
    }

    /**
     * 计算 view 坐标以及宽高值, 如果 view 是 ImageView
     * 同时也会计算图片在 ImageView 中实际坐标以及宽高值
     * @param view View 对象
     * @return Location
     */
    public static Location convertLocation(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        Location oLocation = new Location();
        oLocation.setX(location[0]);
        oLocation.setY(location[1]);
        oLocation.setWidth(view.getWidth());
        oLocation.setHeight(view.getHeight());

        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            int intriWidth = drawable.getIntrinsicWidth();
            int intriHeight = drawable.getIntrinsicHeight();
            if (view.getWidth() > intriWidth) {
                oLocation.setRealX(location[0] + (view.getWidth() - intriWidth) / 2);
                oLocation.setRealWidth(intriWidth);
            } else {
                oLocation.setRealX(location[0]);
                oLocation.setRealWidth(view.getWidth());
            }
            if (view.getHeight() > intriHeight) {
                oLocation.setRealY(location[1] + (view.getHeight() - intriHeight) / 2);
                oLocation.setRealHeight(intriHeight);
            } else {
                oLocation.setRealY(location[1]);
                oLocation.setRealHeight(view.getHeight());
            }
        }

        return oLocation;
    }
}
