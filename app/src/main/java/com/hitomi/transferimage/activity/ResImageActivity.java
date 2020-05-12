package com.hitomi.transferimage.activity;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
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
import com.hitomi.transferimage.ImageConfig;
import com.hitomi.transferimage.R;
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
public class ResImageActivity extends BaseActivity {
    private RecyclerView rvImages;

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new LinearLayoutManager(this));
        rvImages.setAdapter(new FriendsCircleAdapter());
    }

    @Override
    protected void testTransferee() {
    }

    /**
     * 朋友圈列表数据适配器
     */
    private class FriendsCircleAdapter extends CommonAdapter<Uri> {
        private PhotosAdapter photosAdapter = new PhotosAdapter(
                ResImageActivity.this,
                R.layout.item_image,
                ImageConfig.getResUriList(ResImageActivity.this)
        );
        private DividerGridItemDecoration divider = new DividerGridItemDecoration(
                Color.TRANSPARENT,
                ConvertUtils.dp2px(8f),
                ConvertUtils.dp2px(8f)
        );

        FriendsCircleAdapter() {
            super(ResImageActivity.this, R.layout.item_friends_circle, ImageConfig.getResUriList(ResImageActivity.this));
            // 设置朋友圈图片点击事件
            photosAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    TransferConfig config = TransferConfig.build()
                            .setSourceUriList(getDatas())
                            .setProgressIndicator(new ProgressBarIndicator())
                            .setIndexIndicator(new NumberIndexIndicator())
                            .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                            .setNowThumbnailIndex(position)
                            .bindRecyclerView(((RecyclerView) view.getParent()), R.id.iv_thum);
                    transferee.apply(config).show();
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int i) {
                    return false;
                }
            });
        }

        @Override
        protected void convert(ViewHolder viewHolder, Uri item, final int position) {
            final RecyclerView rvPhotos = viewHolder.getView(R.id.rv_photos);
            // 重置 divider
            rvPhotos.removeItemDecoration(divider);
            rvPhotos.addItemDecoration(divider);
            if (rvPhotos.getLayoutManager() == null)
                rvPhotos.setLayoutManager(new GridLayoutManager(ResImageActivity.this, 3));
            if (rvPhotos.getAdapter() == null)
                rvPhotos.setAdapter(photosAdapter);
        }
    }

    /**
     * 单个 item 中照片数据适配器
     */
    private static class PhotosAdapter extends CommonAdapter<Uri> {

        PhotosAdapter(Context context, int layoutId, List<Uri> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(final ViewHolder holder, Uri uri, final int position) {
            ImageView imageView = holder.getView(R.id.iv_thum);
            Glide.with(imageView)
                    .load(uri)
                    .placeholder(R.mipmap.ic_empty_photo)
                    .into(imageView);
        }
    }
}
