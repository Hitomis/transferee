package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.TransferImage;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends BaseActivity {

    private GridView gvImages;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        transferImage = TransferImage.getDefault(this);
        gvImages = (GridView) findViewById(R.id.gv_images);

        gvImages.setAdapter(new CommonAdapter<String>(this, R.layout.item_grid_image, imageStrList) {
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
                        new TransferImage.Builder(GridViewActivity.this)
                                .setBackgroundColor(Color.parseColor("#EE009999"))
                                .setOriginImageList(wrapOriginImageViewList())
                                .setImageUrlList(imageStrList)
                                .setOriginIndex(position)
                                .setup(transferImage)
                                .show();

                    }
                });
            }
        });
    }

    /**
     * 包装缩略图 ImageView 集合
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

}
