package com.hitomi.yifangbao.tilibrary;

import android.graphics.Bitmap;

import com.hitomi.yifangbao.tilibrary.anim.ITransferAnimator;

import java.util.List;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferAttr {

    private int backgroundColor;

    private List<Bitmap> bitmapList;
    private List<String> imageStrList;
    private List<Integer> imageResLsit;

    private ITransferAnimator transferAnima;

    public ITransferAnimator getTransferAnima() {
        return transferAnima;
    }

    public void setTransferAnima(ITransferAnimator transferAnima) {
        this.transferAnima = transferAnima;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public List<Bitmap> getBitmapList() {
        return bitmapList;
    }

    public void setBitmapList(List<Bitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }

    public List<String> getImageStrList() {
        return imageStrList;
    }

    public void setImageStrList(List<String> imageStrList) {
        this.imageStrList = imageStrList;
    }

    public List<Integer> getImageResLsit() {
        return imageResLsit;
    }

    public void setImageResLsit(List<Integer> imageResLsit) {
        this.imageResLsit = imageResLsit;
    }
}
