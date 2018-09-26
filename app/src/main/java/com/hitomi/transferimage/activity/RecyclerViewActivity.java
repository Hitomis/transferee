package com.hitomi.transferimage.activity;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

public class RecyclerViewActivity extends BaseActivity {
    private RecyclerView rvImages;

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        rvImages = (RecyclerView) findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setThumbnailImageList(ImageConfig.getThumbnailPicUrlList())
                .setSourceImageList(ImageConfig.getSourcePicUrlList())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setJustLoadHitImage(true)
                .bindRecyclerView(rvImages, R.id.iv_thum);

        rvImages.setAdapter(new RecyclerViewActivity.NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<String> {
        public NineGridAdapter() {
            super(RecyclerViewActivity.this, R.layout.item_image, ImageConfig.getThumbnailPicUrlList());
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
            ImageLoader.getInstance().displayImage(item, imageView, options);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    config.setNowThumbnailIndex(position);
                    transferee.apply(config).show();
                }
            });
        }
    }
}
