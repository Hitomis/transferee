package com.hitomi.yifangbao.tilibrary.style;

import android.view.View;

/**
 * Created by hitomi on 2017/1/19.
 */

public class Location {

    private int x;

    private int y;

    private int width;

    private int height;

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

    public static Location converLocation(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        Location oLocation = new Location();
        oLocation.setX(location[0]);
        oLocation.setY(location[1]);
        oLocation.setWidth(view.getWidth());
        oLocation.setHeight(view.getHeight());
        return oLocation;
    }
}
