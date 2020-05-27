package com.hitomi.transferimage.activity;

import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.style.index.NumberIndexIndicator;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.SourceConfig;
import com.hitomi.transferimage.divider.DividerGridItemDecoration;
import com.vansz.glideimageloader.GlideImageLoader;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * 仿朋友圈
 * <p>
 * Created by Vans Z on 2020/4/16.
 */
public class FriendsCircleActivity extends BaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        RecyclerView rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new LinearLayoutManager(this));
        rvImages.setAdapter(new FriendsCircleAdapter());
    }

    @Override
    protected void testTransferee() {
    }

    private TransferConfig.Builder getBuilder(int pos) {
        TransferConfig.Builder builder = TransferConfig.build()
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()));
        if (pos == 4) {
            builder.enableHideThumb(false);
        } else if (pos == 5) {
            builder.enableJustLoadHitPage(true);
        } else if (pos == 6) {
            builder.enableDragPause(true);
        }
        return builder;
    }

    /**
     * 朋友圈列表数据适配器
     */
    private class FriendsCircleAdapter extends CommonAdapter<Pair<String, List<String>>> {
        private DividerGridItemDecoration divider = new DividerGridItemDecoration(
                Color.TRANSPARENT,
                ConvertUtils.dp2px(8f),
                ConvertUtils.dp2px(8f)
        );

        FriendsCircleAdapter() {
            super(FriendsCircleActivity.this, R.layout.item_friends_circle, SourceConfig.getFriendsCircleList(FriendsCircleActivity.this));
        }

        @Override
        protected void convert(ViewHolder viewHolder, final Pair<String, List<String>> item, final int position) {
            viewHolder.setText(R.id.tv_content, item.first);
            final RecyclerView rvPhotos = viewHolder.getView(R.id.rv_photos);
            // 重置 divider
            rvPhotos.removeItemDecoration(divider);
            rvPhotos.addItemDecoration(divider);
            if (rvPhotos.getLayoutManager() == null)
                rvPhotos.setLayoutManager(new GridLayoutManager(FriendsCircleActivity.this, 3));
            PhotosAdapter photosAdapter = new PhotosAdapter(
                    FriendsCircleActivity.this,
                    R.layout.item_image,
                    item.second
            );
            photosAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int pos) {
                    transferee.apply(getBuilder(position)
                            .setNowThumbnailIndex(pos)
                            .setSourceUrlList(item.second)
                            .bindRecyclerView(((RecyclerView) view.getParent()), R.id.iv_thum)
                    ).show();
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int i) {
                    return false;
                }
            });
            rvPhotos.setAdapter(photosAdapter);
        }
    }

    /**
     * 单个 item 中照片数据适配器
     */
    private static class PhotosAdapter extends CommonAdapter<String> {

        PhotosAdapter(Context context, int layoutId, List<String> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(final ViewHolder holder, String url, final int position) {
            ImageView imageView = holder.getView(R.id.iv_thum);
            Glide.with(imageView)
                    .load(url)
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);
        }
    }
}
