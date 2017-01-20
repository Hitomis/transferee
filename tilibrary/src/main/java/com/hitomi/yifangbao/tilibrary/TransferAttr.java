package com.hitomi.yifangbao.tilibrary;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;

import java.util.List;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferAttr {

    private List<ImageView> originImageList;
    private int originCurrIndex;
    private int backgroundColor;

    private List<Bitmap> bitmapList;
    private List<String> imageStrList;
    private List<Integer> imageResList;

    private ITransferAnimator transferAnima;
    private boolean localLoad;

    public List<ImageView> getOriginImageList() {
        return originImageList;
    }

    public void setOriginImageList(List<ImageView> originImageList) {
        this.originImageList = originImageList;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (backgroundColor == 0) {
            this.backgroundColor = Color.BLACK;
        } else {
            this.backgroundColor = backgroundColor;
        }
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

    public List<Integer> getImageResList() {
        return imageResList;
    }

    public void setImageResList(List<Integer> imageResList) {
        this.imageResList = imageResList;
    }

    public ITransferAnimator getTransferAnima() {
        return transferAnima;
    }

    public void setTransferAnima(ITransferAnimator transferAnima) {
        this.transferAnima = transferAnima;
    }

    public int getImageSize() {
        return 1;
    }

    public int getOriginCurrIndex() {
        return originCurrIndex;
    }

    public void setOriginCurrIndex(int originCurrIndex) {
        this.originCurrIndex = originCurrIndex;
    }

    public boolean isLocalLoad() {
        return localLoad;
    }
}
