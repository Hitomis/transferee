package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.view.View;

import com.hitomi.transferimage.R;
import com.vansz.glideimageloader.GlideImageLoader;
import com.vansz.picassoimageloader.PicassoImageLoader;
import com.vansz.universalimageloader.UniversalImageLoader;

public class MainActivity extends BaseActivity {
    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        findViewById(R.id.btn_universal_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebPicSimpleActivity.class));
            }
        });

        findViewById(R.id.btn_universal_no_thum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebPicMultiActivity.class));
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
                UniversalImageLoader.with(getApplicationContext()).clearCache();
                GlideImageLoader.with(getApplicationContext()).clearCache();
                PicassoImageLoader.with(getApplicationContext()).clearCache();
            }
        });
    }

    @Override
    protected void testTransferee() {}
}
