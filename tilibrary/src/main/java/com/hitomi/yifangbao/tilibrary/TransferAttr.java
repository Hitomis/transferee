package com.hitomi.yifangbao.tilibrary;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IIndexIndicator;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;
import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;

import java.util.List;

/**
 * Created by hitomi on 2017/1/19.
 */

public class TransferAttr {

    private List<ImageView> originImageList;
    private int currOriginIndex, currShowIndex;
    private int backgroundColor;

    private List<Bitmap> bitmapList;
    private List<Integer> imageResList;
    private List<String> imageStrList;

    private ITransferAnimator transferAnima;
    private IProgressIndicator progressIndicator;
    private IIndexIndicator indexIndicator;
    private ImageLoader imageLoader;

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
        return imageStrList.size();
    }

    public boolean isLocalLoad() {
        return false;
    }

    public IProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(IProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public IIndexIndicator getIndexIndicator() {
        return indexIndicator;
    }

    public void setIndexIndicator(IIndexIndicator indexIndicator) {
        this.indexIndicator = indexIndicator;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public int getCurrOriginIndex() {
        return currOriginIndex;
    }

    public void setCurrOriginIndex(int currOriginIndex) {
        if (currOriginIndex >= originImageList.size()) return ;
        this.currOriginIndex = currOriginIndex;
    }

    int getCurrShowIndex() {
        return currShowIndex;
    }

    void setCurrShowIndex(int currShowIndex) {
        this.currShowIndex = currShowIndex;
    }

    ImageView getCurrOriginImageView() {
        return originImageList.get(currOriginIndex);
    }

}
