package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.tilibrary.loader.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends BaseActivity {

    public static final String THUM_URL = "http://static.fdc.com.cn/avatar/sns/1486263782969.png@233w_160h_20q";
    public static final String SOURCE_URL = "http://static.fdc.com.cn/avatar/sns/1486263782969.png";

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        findViewById(R.id.btn_universal_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NormalImageActivity.class));
            }
        });

        findViewById(R.id.btn_universal_no_thum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoThumActivity.class));
            }
        });

        findViewById(R.id.btn_universal_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocalImageActivity.class));
            }
        });

        findViewById(R.id.btn_recycler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecyclerViewActivity.class));
            }
        });

        findViewById(R.id.btn_clear_universal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transferee.clear(UniversalImageLoader.with(getApplicationContext()));
            }
        });

        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        ImageLoader.getInstance().displayImage(THUM_URL, imageView, options);
        config = TransferConfig.build()
                .bindImageView(imageView, THUM_URL, SOURCE_URL);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(config).show();
            }
        });
    }

    @Override
    protected void testTransferee() {

    }
}
