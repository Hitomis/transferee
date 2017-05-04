package com.hitomi.transferimage.activity.loadertest;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hitomi.tilibrary.TransferConfig;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;
import com.hitomi.universalloader.UniversalImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class UniversalLoaderActivity extends BaseActivity {
    private GridView gvImages;
    private DisplayImageOptions options;
    private List<String> imageStrList;
    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_universal_loader;
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

        gvImages.setAdapter(new UniversalLoaderActivity.NineGridAdapter());
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
            super(UniversalLoaderActivity.this, R.layout.item_grid_image, imageStrList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.image_view);
            ImageLoader.getInstance().displayImage(item, imageView, options);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferConfig config = TransferConfig.build()
                            .setNowThumbnailIndex(position)
                            .setThumbnailImageList(imageStrList)
                            .setSourceImageList(imageStrList)
                            .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                            .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                            .setOriginImageList(wrapOriginImageViewList())
                            .setProgressIndicator(new ProgressPieIndicator())
                            .setImageLoader(UniversalImageLoader.with(getApplicationContext()))
                            .create();
                    transferee.apply(config).show();
                }
            });
        }
    }
}
