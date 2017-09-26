package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.glide.GlideLocalActivity;
import com.hitomi.transferimage.activity.glide.GlideNoThumActivity;
import com.hitomi.transferimage.activity.glide.GlideNormalActivity;
import com.hitomi.transferimage.activity.glide.TouchMoveActivity;
import com.hitomi.transferimage.activity.universal.UniversalLocalActivity;
import com.hitomi.transferimage.activity.universal.UniversalNoThumActivity;
import com.hitomi.transferimage.activity.universal.UniversalNormalActivity;
import com.hitomi.universalloader.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_glide_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GlideNormalActivity.class));
            }
        });

        findViewById(R.id.btn_glide_no_thum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GlideNoThumActivity.class));
            }
        });

        findViewById(R.id.btn_touch_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TouchMoveActivity.class));
            }
        });

        findViewById(R.id.btn_glide_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GlideLocalActivity.class));
            }
        });

        findViewById(R.id.btn_universal_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalNormalActivity.class));
            }
        });

        findViewById(R.id.btn_universal_no_thum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalNoThumActivity.class));
            }
        });

        findViewById(R.id.btn_universal_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalLocalActivity.class));
            }
        });

        findViewById(R.id.btn_clear_glide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Transferee.clear(GlideImageLoader.with(getApplicationContext()));
            }
        });

        findViewById(R.id.btn_clear_universal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transferee.clear(UniversalImageLoader.with(getApplicationContext()));
            }
        });

    }
}
