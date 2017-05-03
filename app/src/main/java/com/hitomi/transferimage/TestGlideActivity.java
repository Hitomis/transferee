package com.hitomi.transferimage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.hitomi.glideloader.GlideProgressSupport.ProgressTarget;

public class TestGlideActivity extends AppCompatActivity {

    private Button btnTest1, btnTest2;
    private ImageView image1, image2;

    private static final String url = "http://static.fdc.com.cn/avatar/sns/1486263697527.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_glide);

        btnTest1 = (Button) findViewById(R.id.btn_test1);
        btnTest2 = (Button) findViewById(R.id.btn_test2);

        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);

        btnTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressTarget<String, GlideDrawable> progressTarget =
                        new ProgressTarget<String, GlideDrawable>(url, new GlideDrawableImageViewTarget(image1)) {

                            @Override
                            protected void onStartDownload() {}

                            @Override
                            protected void onDownloading(long bytesRead, long expectedLength) {}

                            @Override
                            protected void onDownloaded() {}

                            @Override
                            protected void onDelivered(int status) {}
                        };

                Glide.with(TestGlideActivity.this)
                        .load(url)
                        .dontAnimate()
                        .into(progressTarget);
            }
        });

        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressTarget<String, GlideDrawable> progressTarget =
                        new ProgressTarget<String, GlideDrawable>(url, new GlideDrawableImageViewTarget(new ImageView(TestGlideActivity.this))) {

                            @Override
                            protected void onStartDownload() {}

                            @Override
                            protected void onDownloading(long bytesRead, long expectedLength) {}

                            @Override
                            protected void onDownloaded() {}

                            @Override
                            protected void onDelivered(int status) {}

                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                image2.setImageDrawable(resource);
                            }
                        };

                Glide.with(TestGlideActivity.this)
                        .load(url)
                        .dontAnimate()
                        .into(progressTarget);
            }
        });
    }
}
