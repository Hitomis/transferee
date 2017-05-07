package com.hitomi.transferimage.activity.styletest;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import java.util.List;

public class GridViewActivity extends BaseActivity {

    private GridView gvImages;
    private List<String> imageStrList;

    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/c3e4308aa6e2074c77d343d8824179c0-1024x628.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/d86a1428bea533217c7e2b13b4e5963e-1024x736.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/5cf47d87616dba975d1e85214025c349-1024x676.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/53605f987ec71b74bb376b47a238430a-1024x734.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/dad7c7f0e646780774de0374f406014b-1024x820.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/cbdef59bd87caa712abb0f144c463101-1024x833.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/e0f944e4c73ad5b6a018029bc7ebbb37-1024x734.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/eadc61ec6a819fe8fb518f06b37b5ccf-1024x668.jpg");
        imageStrList.add("http://oxgood.com/wp-content/uploads/2016/07/e41b862ee191d342ec80cf39f97cc067-1024x724.jpg");

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
            super(GridViewActivity.this, R.layout.item_grid_image, imageStrList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            ImageView imageView = viewHolder.getView(R.id.image_view);

            Glide.with(GridViewActivity.this)
                    .load(item)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransferConfig config = TransferConfig.build()
                            .setNowThumbnailIndex(position)
                            .setSourceImageList(imageStrList)
                            .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                            .setOriginImageList(wrapOriginImageViewList())
                            .setProgressIndicator(new ProgressPieIndicator())
                            .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                            .create();
                    transferee.apply(config).show(new Transferee.OnTransfereeChangeListener() {
                        @Override
                        public void onShow() {
                            Glide.with(GridViewActivity.this).pauseRequests();
                        }

                        @Override
                        public void onDismiss() {
                            Glide.with(GridViewActivity.this).resumeRequests();
                        }
                    });
                }
            });
        }
    }

}
