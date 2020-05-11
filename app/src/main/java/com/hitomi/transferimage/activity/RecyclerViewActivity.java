package com.hitomi.transferimage.activity;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
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
    private HeaderAndFooterWrapper headerAndFooterWrapper;
    private LinearLayoutManager linLayManager = new LinearLayoutManager(this);
    private GridLayoutManager gridLayManager = new GridLayoutManager(this, 3);

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(gridLayManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle) {
            if (rvImages.getLayoutManager() == linLayManager) {
                rvImages.setLayoutManager(gridLayManager);
            } else {
                rvImages.setLayoutManager(linLayManager);
            }
            headerAndFooterWrapper.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void testTransferee() {
        config = TransferConfig.build()
                .setSourceImageList(ImageConfig.getSourcePicUrlList())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setImageLoader(PicassoImageLoader.with(getApplicationContext()))
                .setJustLoadHitImage(true)
                .bindRecyclerView(rvImages, 1, 1, R.id.iv_thum);


        NineGridAdapter adapter = new RecyclerViewActivity.NineGridAdapter();
        headerAndFooterWrapper = new HeaderAndFooterWrapper(adapter);

        TextView t1 = new TextView(this);
        t1.setGravity(Gravity.CENTER);
        t1.setText("我是 RecyclerView 的 Header ");
        TextView t2 = new TextView(this);
        t2.setText("我是 RecyclerView 的 footer");
        t2.setGravity(Gravity.CENTER);
        headerAndFooterWrapper.addHeaderView(t1);
        headerAndFooterWrapper.addFootView(t2);

        rvImages.setAdapter(headerAndFooterWrapper);
        headerAndFooterWrapper.notifyDataSetChanged();
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
                    // position 减去 header 的数量后，才是当前图片正确的索引
                    config.setNowThumbnailIndex(position - 1);
                    transferee.apply(config).show();
                }
            });
        }
    }
}
