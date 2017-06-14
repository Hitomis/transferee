package com.hitomi.transferimage.activity.universal;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;
import com.hitomi.universalloader.UniversalImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;

public class UniversalNoThumActivity extends BaseActivity {
    private DisplayImageOptions options;

    {
        sourceImageList = new ArrayList<>();
        sourceImageList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/mcofEgbkwpuvlob.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/DWABSHbkCdElEDA.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i2.17173cdn.com/2fhnvk/YWxqaGBf/cms3/xNLzqtbkCdElEyC.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/vUFNHwbljxruktB.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/FNGGjrbljxrukba.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/Juvmrjbkwpuvjyv.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i1.17173cdn.com/2fhnvk/YWxqaGBf/cms3/qlwPVQbkwpuvmcc.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i3.17173cdn.com/2fhnvk/YWxqaGBf/cms3/vMbgOlbkwpuvlEE.jpg!a-3-640x.jpg");
        sourceImageList.add("http://i2.17173cdn.com/2fhnvk/YWxqaGBf/cms3/GJhaLjbkwpuvlty.jpg!a-3-640x.jpg");
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
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        options = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(R.mipmap.ic_empty_photo)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        gvImages.setAdapter(new UniversalNoThumActivity.NineGridAdapter());
    }

    private class NineGridAdapter extends CommonAdapter<String> {

        public NineGridAdapter() {
            super(UniversalNoThumActivity.this, R.layout.item_grid_image, sourceImageList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            ImageView imageView = viewHolder.getView(R.id.image_view);
            ImageLoader.getInstance().displayImage(item, imageView, options);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferConfig config = TransferConfig.build()
                            .setNowThumbnailIndex(position)
                            .setSourceImageList(sourceImageList)
                            .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                            .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                            .setOriginImageList(wrapOriginImageViewList(sourceImageList.size()))
                            .setProgressIndicator(new ProgressBarIndicator())
                            .setIndexIndicator(new NumberIndexIndicator())
                            .setImageLoader(UniversalImageLoader.with(getApplicationContext()))
                            .setJustLoadHitImage(true)
                            .create();
                    transferee.apply(config).show();
                }
            });
        }
    }

}
