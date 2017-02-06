package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.yifangbao.tilibrary.TransferImage;
import com.hitomi.yifangbao.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.yifangbao.tilibrary.style.anim.TransitionAnimator;
import com.hitomi.yifangbao.tilibrary.style.index.IndexCircleIndicator;
import com.hitomi.yifangbao.tilibrary.style.progress.ProgressPieIndicator;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends AppCompatActivity {
    private GridView gridView;

    private List<String> imageStrList;

    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486263697527.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486263820142.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1485136117467.jpg");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1485053874297.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png");
        imageStrList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new CommonAdapter<String>(this, R.layout.item_image, imageStrList) {
            @Override
            protected void convert(ViewHolder viewHolder, String item, final int position) {
                final ImageView imageView = viewHolder.getView(R.id.image_view);
                imageView.setClickable(true);
                Glide.with(GridViewActivity.this)
                        .load(item)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<ImageView> originImageList = new ArrayList<>();
                        originImageList.add(imageView);

                        List<String> imageList = new ArrayList<>();
                        imageList.add(imageStrList.get(position));

                        TransferImage transferLayout = new TransferImage.Builder(GridViewActivity.this)
                                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                                .setTransferAnima(new TransitionAnimator())
                                .setProgressIndicator(new ProgressPieIndicator())
                                .setIndexIndicator(new IndexCircleIndicator())
                                .setBackgroundColor(Color.BLACK)
                                .setImageStrList(imageList)
                                .setOriginImageList(originImageList)
                                .setOriginIndex(0)
                                .setOffscreenPageLimit(1)
                                .create();
                        transferLayout.show();
                    }
                });
            }
        });
    }
}
