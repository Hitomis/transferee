package com.hitomi.transferimage.activity;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
import com.vansz.glideimageloader.GlideImageLoader;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

public class WebPicSimpleActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.activity_grid_view;
    }

    @Override
    protected void initView() {
        findViewById(R.id.single_layout).setVisibility(View.VISIBLE);
        gvImages = findViewById(R.id.gv_images);

        final ImageView sourceIv = findViewById(R.id.iv_source);
        Glide.with(sourceIv).load(ImageConfig.WEB_URL).into(sourceIv);
        sourceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(TransferConfig.build()
                        .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                        .bindImageView(sourceIv, ImageConfig.WEB_URL)
                ).show();
            }
        });
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setSourceImageList(ImageConfig.getSourcePicUrlList())
                .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .setJustLoadHitImage(true)
                .setOnLongClcikListener(new Transferee.OnTransfereeLongClickListener() {
                    @Override
                    public void onLongClick(ImageView imageView, String imageUri, int pos) {
                        saveImageFile(imageUri);
                    }
                })
                .bindListView(gvImages, R.id.iv_thum);

        gvImages.setAdapter(new WebPicSimpleActivity.NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(WebPicSimpleActivity.this, R.layout.item_image, ImageConfig.getThumbnailPicUrlList());
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    config.setNowThumbnailIndex(position);
                    transferee.apply(config).show();
                }
            });
            Glide.with(imageView).load(item).into(imageView);
        }
    }

}
