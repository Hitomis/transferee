package com.hitomi.transferimage.activity;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
import com.vansz.glideimageloader.GlideImageLoader;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

/**
 * Created by Vans Z on 2020/4/16.
 */
public class ResImageActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.activity_grid_view;
    }

    @Override
    protected void initView() {
        gvImages = findViewById(R.id.gv_images);
        gvImages.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setSourceUriList(ImageConfig.getResUriList(this))
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .bindListView(gvImages, R.id.iv_thum);
        gvImages.setAdapter(new NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<Uri> {

        public NineGridAdapter() {
            super(ResImageActivity.this, R.layout.item_image, ImageConfig.getResUriList(ResImageActivity.this));
        }

        @Override
        protected void convert(ViewHolder viewHolder, Uri item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    config.setNowThumbnailIndex(position);
                    transferee.apply(config).show();
                }
            });
            Glide.with(imageView)
                    .load(item)
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);
        }
    }
}
