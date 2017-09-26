package com.hitomi.transferimage.activity.glide;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
//import com.hitomi.glideloader.GlideImageLoader;
import com.bumptech.glide.request.target.Target;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;
import com.hitomi.universalloader.UniversalImageLoader;
import com.wepie.glide4loader.Glide4ImageLoader;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class GlideNoThumActivity extends BaseActivity {

    private RequestOptions options;

    {
        sourceImageList = new ArrayList<>();
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/d38274f15c.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/061548f1fb.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/4a85dd9bd9.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/a6c57f438d.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201706/9999/b6ae25c618.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/lua4uwojfds.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/4hp4d1fcocu.jpg");
        sourceImageList.add("http://t2.27270.com/uploads/tu/201612/562/d2madqozild.jpg");
        sourceImageList.add("http://ww1.sinaimg.cn/large/9be2329dgw1etlyb1yu49j20c82p6qc1.jpg");

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
        config = TransferConfig.build()
                .setSourceImageList(sourceImageList)
                .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                .setErrorPlaceHolder(R.mipmap.ic_empty_photo)
                .setProgressIndicator(new ProgressPieIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setJustLoadHitImage(true)
                .setImageLoader(Glide4ImageLoader.with(getApplicationContext()))
                .setOnLongClcikListener(new Transferee.OnTransfereeLongClickListener() {
                    @Override
                    public void onLongClick(ImageView imageView, int pos) {
                        saveImageByUniversal(imageView);
                    }
                })
                .create();
        options = new RequestOptions().centerCrop()
                .placeholder(R.mipmap.ic_empty_photo);

        gvImages.setAdapter(new NineGridAdapter());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != WRITE_EXTERNAL_STORAGE) {
            Toast.makeText(this, "请允许获取相册图片文件写入权限", Toast.LENGTH_SHORT).show();
        }
    }

    private class NineGridAdapter extends CommonAdapter<String> {


        public NineGridAdapter() {
            super(GlideNoThumActivity.this, R.layout.item_grid_image, sourceImageList);
        }

        @Override
        protected void convert(ViewHolder viewHolder, final String item, final int position) {
            ImageView imageView = viewHolder.getView(R.id.image_view);

            Glide.with(GlideNoThumActivity.this)
                    .load(item)
                    .apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Executors.newSingleThreadExecutor().submit(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences loadSharedPref = getSharedPreferences(
                                            "transferee", Context.MODE_PRIVATE);
                                    Set<String> loadedSet = loadSharedPref.getStringSet("load_set", new HashSet<String>());
                                    if (!loadedSet.contains(item)) {
                                        loadedSet.add(item);

                                        loadSharedPref.edit()
                                                .clear() // SharedPreferences 关于 putStringSet 的 bug 修复方案
                                                .putStringSet("load_set", loadedSet)
                                                .apply();
                                    }
                                }
                            });
                            return false;
                        }
                    })
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    config.setNowThumbnailIndex(position);
                    config.setOriginImageList(wrapOriginImageViewList(sourceImageList.size()));

                    transferee.apply(config).show(new Transferee.OnTransfereeStateChangeListener() {
                        @Override
                        public void onShow() {
                            Glide.with(GlideNoThumActivity.this).pauseRequests();
                        }

                        @Override
                        public void onDismiss() {
                            Glide.with(GlideNoThumActivity.this).resumeRequests();
                        }
                    });
                }
            });
        }
    }

}
