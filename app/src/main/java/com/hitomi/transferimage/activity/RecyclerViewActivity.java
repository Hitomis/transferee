package com.hitomi.transferimage.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
import com.squareup.picasso.Picasso;
import com.vansz.picassoimageloader.PicassoImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

/**
 * 使用 PicassoImageLoader 演示
 */
public class RecyclerViewActivity extends BaseActivity {
    private RecyclerView rvImages;

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setSourceImageList(ImageConfig.getSourcePicUrlList())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(PicassoImageLoader.with(getApplicationContext()))
                .setJustLoadHitImage(true)
                .bindRecyclerView(rvImages, R.id.iv_thum);


        NineGridAdapter adapter = new RecyclerViewActivity.NineGridAdapter();
        HeaderAndFooterWrapper mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(adapter);

        TextView t1 = new TextView(this);
        t1.setText("我是 Header 1");
        TextView t2 = new TextView(this);
        t2.setText("我是 Header 2");
        mHeaderAndFooterWrapper.addHeaderView(t1);
        mHeaderAndFooterWrapper.addHeaderView(t2);

        rvImages.setAdapter(mHeaderAndFooterWrapper);
        mHeaderAndFooterWrapper.notifyDataSetChanged();
    }

    private class NineGridAdapter extends CommonAdapter<String> {
        public NineGridAdapter() {
            super(RecyclerViewActivity.this, R.layout.item_image, ImageConfig.getThumbnailPicUrlList());
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
            Picasso.get()
                    .load(item)
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    config.setNowThumbnailIndex(position - 2);
                    transferee.apply(config).show();
                }
            });
        }
    }
}
