package com.hitomi.transferimage.activity;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

public class NormalImageActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.activity_grid_view;
    }

    @Override
    protected void initView() {
        findViewById(R.id.single_layout).setVisibility(View.VISIBLE);
        gvImages = (GridView) findViewById(R.id.gv_images);

        final ImageView thumIv = (ImageView) findViewById(R.id.iv_thum);
        final ImageView sourceIv = (ImageView) findViewById(R.id.iv_source);

        ImageLoader.getInstance().displayImage(ImageConfig.THUM_URL, thumIv, options);
        ImageLoader.getInstance().displayImage(ImageConfig.WEB_URL, sourceIv, options);

        thumIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(TransferConfig.build().bindImageView
                        (thumIv, ImageConfig.THUM_URL, ImageConfig.SOURCE_URL)).show();
            }
        });

        sourceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(TransferConfig.build().bindImageView
                        (sourceIv, ImageConfig.WEB_URL)).show();
            }
        });
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setThumbnailImageList(ImageConfig.getThumbnailPicUrlList())
                .setSourceImageList(ImageConfig.getSourcePicUrlList())
                .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                .setProgressIndicator(new ProgressPieIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setJustLoadHitImage(true)
                .setOnLongClcikListener(new Transferee.OnTransfereeLongClickListener() {
                    @Override
                    public void onLongClick(ImageView imageView, int pos) {
                        saveImageByUniversal(imageView);
                    }
                })
                .bindListView(gvImages, R.id.iv_thum);

        gvImages.setAdapter(new NormalImageActivity.NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(NormalImageActivity.this, R.layout.item_image, ImageConfig.getThumbnailPicUrlList());
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
            ImageLoader.getInstance().displayImage(item, imageView, options);
        }
    }

}
