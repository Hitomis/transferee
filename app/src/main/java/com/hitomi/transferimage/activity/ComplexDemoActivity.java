package com.hitomi.transferimage.activity;

import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.SourceConfig;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vansz.glideimageloader.GlideImageLoader;
import com.vansz.universalimageloader.UniversalImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

public class ComplexDemoActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private GridView gridView;
    private ImageView imageView;
    private Button button;

    @Override
    public int getContentView() {
        return R.layout.activity_complex_demo;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.rv_transferee);
        gridView = findViewById(R.id.gv_transferee);
        imageView = findViewById(R.id.iv_single_view);
        button = findViewById(R.id.btn_none_view);
    }

    @Override
    protected void testTransferee() {
        recyclerDemo();
        gridDemo();
        singleViewDemo();
        noneViewDemo();
    }

    private void noneViewDemo() {
        button.setOnClickListener(v -> Transferee.getDefault(this).apply(TransferConfig.build()
                .setImageLoader(UniversalImageLoader.with(getApplicationContext()))
                .setSourceUrlList(SourceConfig.getMixingSourceGroup())
                .create()
        ).show());
    }

    private void singleViewDemo() {
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        ImageLoader.getInstance().displayImage(SourceConfig.getMixingSourceGroup().get(0), imageView);
        imageView.setOnClickListener(v -> {
            Transferee.getDefault(this).apply(TransferConfig.build()
                    .setSourceUrlList(SourceConfig.getMixingSourceGroup())
                    .setImageLoader(UniversalImageLoader.with(getApplicationContext()))
                    .enableJustLoadHitPage(true)
                    .setCustomView(View.inflate(getBaseContext(), R.layout.layout_custom, null))
                    .bindImageView(imageView)
            ).show();
        });
    }

    private void gridDemo() {
        final TransferConfig gridTransConfig = TransferConfig.build()
                .setSourceUrlList(SourceConfig.getMixingSourceGroup())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .enableScrollingWithPageChange(true)
                .bindListView(gridView, R.id.iv_thum);
        gridView.setAdapter(new GridAdapter());
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            gridTransConfig.setNowThumbnailIndex(position);
            transferee.apply(gridTransConfig).show();
        });
    }

    private void recyclerDemo() {
        final TransferConfig recyclerTransConfig = TransferConfig.build()
                .setSourceUrlList(SourceConfig.getOriginalSourceGroup())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .enableHideThumb(false)
                .bindRecyclerView(recyclerView, R.id.iv_thum);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter();
        recyclerAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int pos) {
                recyclerTransConfig.setNowThumbnailIndex(pos);
                transferee.apply(recyclerTransConfig).show();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int i) {
                return false;
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(recyclerAdapter);
    }

    private class RecyclerAdapter extends CommonAdapter<String> {
        RecyclerAdapter() {
            super(ComplexDemoActivity.this, R.layout.item_image, SourceConfig.getThumbSourceGroup());
        }

        @Override
        protected void convert(ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
            Glide.with(imageView)
                    .load(item)
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);
        }
    }

    private class GridAdapter extends com.zhy.adapter.abslistview.CommonAdapter<String> {

        GridAdapter() {
            super(ComplexDemoActivity.this, R.layout.item_image, SourceConfig.getMixingSourceGroup());
        }

        @Override
        protected void convert(com.zhy.adapter.abslistview.ViewHolder viewHolder, String item, final int position) {
            final ImageView imageView = viewHolder.getView(R.id.iv_thum);
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
