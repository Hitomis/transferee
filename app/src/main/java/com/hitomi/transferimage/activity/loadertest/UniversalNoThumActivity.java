package com.hitomi.transferimage.activity.loadertest;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import java.util.List;

public class UniversalNoThumActivity extends BaseActivity {

    private GridView gvImages;
    private DisplayImageOptions options;
    private List<String> sourceImageList;
    {
        sourceImageList = new ArrayList<>();
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/c3e4308aa6e2074c77d343d8824179c0-1024x628.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/d86a1428bea533217c7e2b13b4e5963e-1024x736.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/5cf47d87616dba975d1e85214025c349-1024x676.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/53605f987ec71b74bb376b47a238430a-1024x734.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/dad7c7f0e646780774de0374f406014b-1024x820.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/cbdef59bd87caa712abb0f144c463101-1024x833.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/e0f944e4c73ad5b6a018029bc7ebbb37-1024x734.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/eadc61ec6a819fe8fb518f06b37b5ccf-1024x668.jpg");
        sourceImageList.add("http://oxgood.com/wp-content/uploads/2016/07/e41b862ee191d342ec80cf39f97cc067-1024x724.jpg");
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

        gvImages.setAdapter(new UniversalNoThumActivity.NineGridAdapter());
    }

    /**
     * 包装缩略图 ImageView 集合
     *
     * @return
     */
    @NonNull
    private List<ImageView> wrapOriginImageViewList() {
        List<ImageView> originImgList = new ArrayList<>();
        for (int i = 0; i < sourceImageList.size(); i++) {
            ImageView thumImg = (ImageView) ((LinearLayout) gvImages.getChildAt(i)).getChildAt(0);
            originImgList.add(thumImg);
        }
        return originImgList;
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
                            .setOriginImageList(wrapOriginImageViewList())
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
