package com.hitomi.transferimage.activity;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.SourceConfig;
import com.hitomi.transferimage.R;
import com.vansz.glideimageloader.GlideImageLoader;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

/**
 * 使用 GlideImageLoader 演示
 */
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
        Glide.with(sourceIv)
                .load(SourceConfig.getSourcePicUrlList().get(0))
                .placeholder(R.mipmap.ic_empty_photo)
                .into(sourceIv);
        sourceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(TransferConfig.build()
                        .setSourceUrlList(SourceConfig.getSourcePicUrlList())
                        .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                        .enableJustLoadHitPage(true)
                        .setCustomView(View.inflate(getBaseContext(), R.layout.layout_custom, null))
                        .bindImageView(sourceIv)
                ).show();
            }
        });
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setSourceUrlList(SourceConfig.getSourcePicUrlList())
                .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .enableScrollingWithPageChange(true)
                .setOnLongClickListener(new Transferee.OnTransfereeLongClickListener() {
                    @Override
                    public void onLongClick(ImageView imageView, String imageUri, int pos) {
                        saveImageFile(imageUri);
                    }
                })
                .bindListView(gvImages, R.id.iv_thum);

        gvImages.setAdapter(new NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(WebPicSimpleActivity.this, R.layout.item_image, SourceConfig.getThumbnailPicUrlList());
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
            if (item.endsWith(".mp4")) {
                Glide.with(imageView)
                        .load(item)
                        .frame(1000_000)
                        .placeholder(R.mipmap.ic_empty_photo)
                        .into(imageView);
            } else {
                Glide.with(imageView)
                        .load(item)
                        .placeholder(R.mipmap.ic_empty_photo)
                        .into(imageView);
            }

        }
    }

}
