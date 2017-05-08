package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.loadertest.UniversalLoaderActivity;
import com.hitomi.transferimage.activity.loadertest.UniversalNoThumActivity;
import com.hitomi.transferimage.activity.styletest.GridViewActivity;
import com.hitomi.transferimage.activity.styletest.ListViewActivity;
import com.hitomi.transferimage.activity.styletest.TouchMoveActivity;
import com.hitomi.universalloader.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    private Button btnList, btnGrid, btnGoTouchMove, btnClearGlide;
    private Button btnUniversal, btnUniversalNoThum, btnClearUniversal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnList = (Button) findViewById(R.id.btn_list);
        btnGrid = (Button) findViewById(R.id.btn_grid);
        btnGoTouchMove = (Button) findViewById(R.id.btn_touch_move);
        btnClearGlide = (Button) findViewById(R.id.btn_clear_glide);
        btnUniversal = (Button) findViewById(R.id.btn_universal);
        btnUniversalNoThum = (Button) findViewById(R.id.btn_universal_npo_thum);
        btnClearUniversal = (Button) findViewById(R.id.btn_clear_universal);

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListViewActivity.class));
            }
        });

        btnGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GridViewActivity.class));
            }
        });

        btnGoTouchMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TouchMoveActivity.class));
            }
        });

        btnUniversal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalLoaderActivity.class));
            }
        });

        btnUniversalNoThum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalNoThumActivity.class));
            }
        });

        btnClearGlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transferee.clear(GlideImageLoader.with(getApplicationContext()));
            }
        });

        btnClearUniversal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transferee.clear(UniversalImageLoader.with(getApplicationContext()));
            }
        });

    }

}
