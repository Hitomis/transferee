package com.hitomi.yifangbao.tilibrary;

import android.graphics.Color;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IIndexIndicator;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;
import com.hitomi.yifangbao.tilibrary.style.ITransferAnimator;

import java.util.List;

/**
 * Attributes
 * <p>
 * Created by hitomi on 2017/1/19.
 */
class TransferAttr {

    private List<ImageView> originImageList;
    private int currOriginIndex, currShowIndex;
    private int backgroundColor;
    private int offscreenPageLimit;
    private int placeHolder;

    private ITransferAnimator transferAnima;
    private IProgressIndicator progressIndicator;
    private IIndexIndicator indexIndicator;
    private ImageLoader imageLoader;

    private List<String> imageStrList;

    List<ImageView> getOriginImageList() {
        return originImageList;
    }

    void setOriginImageList(List<ImageView> originImageList) {
        this.originImageList = originImageList;
    }

    int getBackgroundColor() {
        return backgroundColor;
    }

    void setBackgroundColor(int backgroundColor) {
        if (backgroundColor == 0) {
            this.backgroundColor = Color.BLACK;
        } else {
            this.backgroundColor = backgroundColor;
        }
    }

    List<String> getImageStrList() {
        return imageStrList;
    }

    void setImageStrList(List<String> imageStrList) {
        this.imageStrList = imageStrList;
    }

    ITransferAnimator getTransferAnima() {
        return transferAnima;
    }

    void setTransferAnima(ITransferAnimator transferAnima) {
        this.transferAnima = transferAnima;
    }

    IProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    void setProgressIndicator(IProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    IIndexIndicator getIndexIndicator() {
        return indexIndicator;
    }

    void setIndexIndicator(IIndexIndicator indexIndicator) {
        this.indexIndicator = indexIndicator;
    }

    ImageLoader getImageLoader() {
        return imageLoader;
    }

    void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    int getCurrOriginIndex() {
        return currOriginIndex;
    }

    void setCurrOriginIndex(int currOriginIndex) {
        if (currOriginIndex >= originImageList.size()) return;
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

    int getOffscreenPageLimit() {
        return offscreenPageLimit;
    }

    void setOffscreenPageLimit(int offscreenPageLimit) {
        this.offscreenPageLimit = offscreenPageLimit;
    }

    int getPlaceHolder() {
        return placeHolder;
    }

    void setPlaceHolder(int placeHolder) {
        this.placeHolder = placeHolder;
    }
}
