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

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.CircleIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.SourceConfig;
import com.vansz.glideimageloader.GlideImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

/**
 * 带 header 、footer 的 RecyclerView 示例
 */
public class HeaderRecyclerActivity extends BaseActivity {
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

        NineGridAdapter adapter = new NineGridAdapter();
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                // position 减去 header 的数量后，才是当前图片正确的索引
                config.setNowThumbnailIndex(position - headerAndFooterWrapper.getHeadersCount());
                transferee.apply(config).show();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int i) {
                return false;
            }
        });

        headerAndFooterWrapper = new HeaderAndFooterWrapper(adapter);
        TextView t1 = new TextView(this);
        t1.setGravity(Gravity.CENTER);
        t1.setText("我是 RecyclerView 的 Header1");
        TextView t2 = new TextView(this);
        t2.setGravity(Gravity.CENTER);
        t2.setText("我是 RecyclerView 的 Header2");
        TextView t3 = new TextView(this);
        t3.setText("我是 RecyclerView 的 footer1");
        t3.setGravity(Gravity.CENTER);
        headerAndFooterWrapper.addHeaderView(t1);
        headerAndFooterWrapper.addHeaderView(t2);
        headerAndFooterWrapper.addFootView(t3);

        rvImages.setAdapter(headerAndFooterWrapper);
        headerAndFooterWrapper.notifyDataSetChanged();
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
                .setSourceUrlList(SourceConfig.getSourcePicUrlList())
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .enableScrollingWithPageChange(true)
                .bindRecyclerView(rvImages, headerAndFooterWrapper.getHeadersCount(),
                        headerAndFooterWrapper.getFootersCount(), R.id.iv_thum);

    }

    private class NineGridAdapter extends CommonAdapter<String> {
        public NineGridAdapter() {
            super(HeaderRecyclerActivity.this, R.layout.item_image, SourceConfig.getSourcePicUrlList());
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
}
