package com.hitomi.transferimage.activity.styletest;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends BaseActivity {

    private GridView gvImages;
    private List<String> imageStrList;

    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/mcofEgbkwpuvlob.jpg!a-3-640x.jpg");
        imageStrList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/DWABSHbkCdElEDA.jpg!a-3-640x.jpg");
        imageStrList.add("http://i2.17173cdn.com/2fhnvk/YWxqaGBf/cms3/xNLzqtbkCdElEyC.jpg!a-3-640x.jpg");
        imageStrList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/vUFNHwbljxruktB.jpg!a-3-640x.jpg");
        imageStrList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/FNGGjrbljxrukba.jpg!a-3-640x.jpg");
        imageStrList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/Juvmrjbkwpuvjyv.jpg!a-3-640x.jpg");
        imageStrList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/qlwPVQbkwpuvmcc.jpg!a-3-640x.jpg");
        imageStrList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/vMbgOlbkwpuvlEE.jpg!a-3-640x.jpg");
        imageStrList.add("http://i2.17173cdn.com/2fhnvk/YWxqaGBf/cms3/GJhaLjbkwpuvlty.jpg!a-3-640x.jpg");

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_grid_view;
    }

    @Override
    protected void initView() {
        gvImages = (GridView) findViewById(R.id.gv_images);
    }

    @Override
    protected void testTransferee() {

        gvImages.setAdapter(new NineGridAdapter());
    }

    /**
     * 包装缩略图 ImageView 集合
     *
     * @return
     */
    @NonNull
    private List<ImageView> wrapOriginImageViewList() {
        List<ImageView> originImgList = new ArrayList<>();
        for (int i = 0; i < imageStrList.size(); i++) {
            ImageView thumImg = (ImageView) ((LinearLayout) gvImages.getChildAt(i)).getChildAt(0);
            originImgList.add(thumImg);
        }
        return originImgList;
    }

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(GridViewActivity.this, R.layout.item_grid_image, imageStrList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            ImageView imageView = viewHolder.getView(R.id.image_view);

            Glide.with(GridViewActivity.this)
                    .load(item)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferConfig config = TransferConfig.build()
                            .setNowThumbnailIndex(position)
                            .setSourceImageList(imageStrList)
                            .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                            .setOriginImageList(wrapOriginImageViewList())
                            .setProgressIndicator(new ProgressPieIndicator())
                            .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                            .create();
                    transferee.apply(config).show(new Transferee.OnTransfereeChangeListener() {
                        @Override
                        public void onShow() {
                            Glide.with(GridViewActivity.this).pauseRequests();
                        }

                        @Override
                        public void onDismiss() {
                            Glide.with(GridViewActivity.this).resumeRequests();
                        }
                    });
                }
            });
        }
    }

}
