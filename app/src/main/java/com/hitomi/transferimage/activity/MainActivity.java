package com.hitomi.transferimage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.glide.GlideNoThumActivity;
import com.hitomi.transferimage.activity.glide.TouchMoveActivity;
import com.hitomi.transferimage.activity.universal.UniversalNoThumActivity;
import com.hitomi.transferimage.activity.universal.UniversalNormalActivity;
import com.hitomi.universalloader.UniversalImageLoader;

public class MainActivity extends AppCompatActivity {

    private Button btnGoTouchMove, btnGlideNoThum;
    private Button btnUniversalNormal, btnUniversalNoThum;
    private Button btnClearGlide, btnClearUniversal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGlideNoThum = (Button) findViewById(R.id.btn_glide_no_thum);

        btnUniversalNormal = (Button) findViewById(R.id.btn_universal_normal);
        btnUniversalNoThum = (Button) findViewById(R.id.btn_universal_no_thum);

        btnGoTouchMove = (Button) findViewById(R.id.btn_touch_move);

        btnClearGlide = (Button) findViewById(R.id.btn_clear_glide);
        btnClearUniversal = (Button) findViewById(R.id.btn_clear_universal);

        btnGlideNoThum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GlideNoThumActivity.class));
            }
        });

        btnUniversalNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalNormalActivity.class));
            }
        });

        btnUniversalNoThum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UniversalNoThumActivity.class));
            }
        });

        btnGoTouchMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TouchMoveActivity.class));
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
