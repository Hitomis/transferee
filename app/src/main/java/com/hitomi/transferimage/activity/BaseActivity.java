package com.hitomi.transferimage.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.tilibrary.transfer.Transferee;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hitomi on 2017/2/13.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int READ_EXTERNAL_STORAGE = 100;

    protected Transferee transferee;
    protected GridView gvImages;
    protected List<String> thumbnailImageList;
    protected List<String> sourceImageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferee = Transferee.getDefault(this);
        setContentView(getContentView());
        initView();
        testTransferee();
    }

    /**
     * 包装缩略图 ImageView 集合
     * <p>
     * 注意：此方法只是为了收集 Activity 列表中所有可见 ImageView 好传递给 transferee。
     * 如果你添加了一些图片路径，扩展了列表图片个数，让列表超出屏幕，导致一些 ImageViwe 不
     * 可见，那么有可能这个方法会报错。这种情况，可以自己根据实际情况，来设置 transferee 的
     * originImageList 属性值
     *
     * @return
     */
    @NonNull
    protected List<ImageView> wrapOriginImageViewList(int size) {
        List<ImageView> originImgList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ImageView thumImg = (ImageView) ((LinearLayout) gvImages.getChildAt(i)).getChildAt(0);
            originImgList.add(thumImg);
        }
        return originImgList;
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void testTransferee();

    @Override
    protected void onDestroy() {
        transferee.destroy();
        super.onDestroy();
    }
}
