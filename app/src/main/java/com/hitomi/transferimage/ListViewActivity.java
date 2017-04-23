package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.hitomi.tilibrary.TransferImage;
import com.hitomi.tilibrary.view.fleximage.FlexImageView;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends BaseActivity {

    private ListView listView;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        transferImage = TransferImage.getDefault(this);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new CommonAdapter<String>(this, R.layout.item_list_image, imageStrList) {

            @Override
            protected void convert(ViewHolder viewHolder, String item, final int position) {
                final FlexImageView imageView = viewHolder.getView(R.id.image_view);
                imageView.setClickable(true);
                Glide.with(ListViewActivity.this)
                        .load(item)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        new TransferImage.Builder(ListViewActivity.this)
//                                .setBackgroundColor(Color.parseColor("#2f008d"))
//                                .setImageUrls(imageStrList.get(position))
//                                .setOriginImages(imageView)
//                                .setup(transferImage)
//                                .show();
                    }
                });
            }
        });
    }

}
