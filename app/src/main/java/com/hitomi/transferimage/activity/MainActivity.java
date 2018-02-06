package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.universal.RecyclerViewActivity;
import com.hitomi.transferimage.activity.universal.UniversalLocalActivity;
import com.hitomi.transferimage.activity.universal.UniversalNoThumActivity;
import com.hitomi.transferimage.activity.universal.UniversalNormalActivity;
import com.hitomi.tilibrary.loader.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }
}
