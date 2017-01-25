package com.hitomi.transferimage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.hitomi.yifangbao.tilibrary.PhotoPreview.PhotoView;
import com.hitomi.yifangbao.tilibrary.loader.ImageLoader;
import com.hitomi.yifangbao.tilibrary.style.IProgressIndicator;
import com.hitomi.yifangbao.tilibrary.style.indicat.ProgressPieIndicator;

import java.io.File;

public class GlideTestActivity extends AppCompatActivity implements ImageLoader.Callback {

    String url1 = "http://img5.niutuku.com/phone/1212/3752/3752-niutuku.com-22310.jpg";
    String url2 = "http://c.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e950e7d5403816385343faf2b4a0.jpg";
    String url3 = "http://e.hiphotos.baidu.com/zhidao/pic/item/7aec54e736d12f2ed5568f4c4dc2d5628535684e.jpg";

    private PhotoView imageView1, imageView2, imageView3, imageView4;

    private IProgressIndicator progressIndicator = new ProgressPieIndicator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_test);
        imageView1 = (PhotoView) findViewById(R.id.image_view1);
        imageView2 = (PhotoView) findViewById(R.id.image_view2);
        imageView3 = (PhotoView) findViewById(R.id.image_view3);
        imageView4 = (PhotoView) findViewById(R.id.image_view4);

        imageView1.enable();
        imageView2.enable();
        imageView3.enable();
        imageView4.enable();


        GlideImageLoader.with(this).loadImage(url1, imageView1, R.drawable.img1, this);
        GlideImageLoader.with(this).loadImage(url2, imageView2, R.drawable.img2, this);
        GlideImageLoader.with(this).loadImage(url3, imageView3, R.drawable.img3, this);

    }

    @Override
    public void onCacheHit(int position, File image) {

    }

    @Override
    public void onCacheMiss(int position, File image) {

    }

    @Override
    public void onStart(int position) {
        progressIndicator.getView(0, (FrameLayout) findViewById(R.id.relay));
    }

    @Override
    public void onProgress(int position, int progress) {
        progressIndicator.onProgress(position, progress);
    }

    @Override
    public void onFinish(int position) {
        progressIndicator.onFinish(0);
    }
}
