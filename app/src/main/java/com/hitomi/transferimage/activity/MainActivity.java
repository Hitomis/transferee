package com.hitomi.transferimage.activity;

import android.content.Intent;

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
        findViewById(R.id.btn_complex_demo).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ComplexDemoActivity.class))
        );

        findViewById(R.id.btn_friends_res).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FriendsCircleActivity.class))
        );
        findViewById(R.id.btn_universal_local).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LocalImageActivity.class))
        );

        findViewById(R.id.btn_recycler).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HeaderRecyclerActivity.class))
        );

        findViewById(R.id.btn_clear_universal).setOnClickListener(v -> {
            UniversalImageLoader.with(getApplicationContext()).clearCache();
            GlideImageLoader.with(getApplicationContext()).clearCache();
            PicassoImageLoader.with(getApplicationContext()).clearCache();
            transferee.clear();
        });
    }

    @Override
    protected void testTransferee() {
    }
}
