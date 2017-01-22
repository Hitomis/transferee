package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hitomi.yifangbao.tilibrary.TransferWindow;
import com.hitomi.yifangbao.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.yifangbao.tilibrary.style.anim.TransitionAnimator;
import com.hitomi.yifangbao.tilibrary.style.indicat.ProgressPieIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Button btnClearFresco, btnClearGlide;

    private ImageView imageView;
    private List<String> imageStrList;

    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://c.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e950e7d5403816385343faf2b4a0.jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(getApplicationContext());
        ImagePipelineFactory.initialize(getApplicationContext());

        btnClearFresco = (Button) findViewById(R.id.btn_clear_fresco);
        btnClearGlide = (Button) findViewById(R.id.btn_clear_glide);

        btnClearFresco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fresco.getImagePipeline().clearCaches();
            }
        });

        btnClearGlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(getApplicationContext()).clearDiskCache();
                        Glide.get(getApplicationContext()).clearMemory();
                    }
                });
            }
        });

        imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setOnTouchListener(new View.OnTouchListener() {

            private float preX, preY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        preX = event.getRawX();
                        preY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float diffX = event.getRawX() - preX;
                        float diffY = event.getRawY() - preY;

                        imageView.setX(imageView.getX() + diffX);
                        imageView.setY(imageView.getY() + diffY);

                        preX = event.getRawX();
                        preY = event.getRawY();
                        break;
                }
                return false;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ImageView> imageViewList = new ArrayList<ImageView>();
                imageViewList.add(imageView);
                TransferWindow transferLayout = new TransferWindow.Builder(MainActivity.this)
                        .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                        .setTransferAnima(new TransitionAnimator())
                        .setProgressIndicator(new ProgressPieIndicator())
                        .setBackgroundColor(Color.BLACK)
                        .setImageStrList(imageStrList)
                        .setOriginImageList(imageViewList)
                        .create();
                transferLayout.show();
            }
        });
    }
}
