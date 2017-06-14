package com.hitomi.transferimage.activity.glide;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

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

public class GlideNoThumActivity extends BaseActivity {

    {
        sourceImageList = new ArrayList<>();
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/d38274f15c.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/061548f1fb.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/4a85dd9bd9.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/a6c57f438d.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/b6ae25c618.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/lua4uwojfds.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/4hp4d1fcocu.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/d2madqozild.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201603/250/gl55chlleh1.jpg");

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

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(GlideNoThumActivity.this, R.layout.item_grid_image, sourceImageList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            ImageView imageView = viewHolder.getView(R.id.image_view);

            Glide.with(GlideNoThumActivity.this)
                    .load(item)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferConfig config = TransferConfig.build()
                            .setNowThumbnailIndex(position)
                            .setSourceImageList(sourceImageList)
                            .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                            .setOriginImageList(wrapOriginImageViewList(sourceImageList.size()))
                            .setProgressIndicator(new ProgressPieIndicator())
                            .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                            .create();
                    transferee.apply(config).show(new Transferee.OnTransfereeStateChangeListener() {
                        @Override
                        public void onShow() {
                            Glide.with(GlideNoThumActivity.this).pauseRequests();
                        }

                        @Override
                        public void onDismiss() {
                            Glide.with(GlideNoThumActivity.this).resumeRequests();
                        }
                    });
                }
            });
        }
    }

}
