package com.hitomi.transferimage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hitomi.transferimage.progress.ProgressImageView;
import com.hitomi.transferimage.progress.ProgressModelLoader;

import java.lang.ref.WeakReference;

public class GlideTestActivity extends AppCompatActivity {

//    private Button btnLoad;
    private ProgressImageView imageView1, imageView2, imageView3, imageView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_test);
//        btnLoad = (Button) findViewById(R.id.btn_load);
        imageView1 = (ProgressImageView) findViewById(R.id.image_view1);
        imageView2 = (ProgressImageView) findViewById(R.id.image_view2);
        imageView3 = (ProgressImageView) findViewById(R.id.image_view3);
        imageView4 = (ProgressImageView) findViewById(R.id.image_view4);


        Glide.with(GlideTestActivity.this)
                .using(new ProgressModelLoader(new ProgressHandler(GlideTestActivity.this, imageView1)))
                .load("http://img5.niutuku.com/phone/1212/3752/3752-niutuku.com-22310.jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.img1)
                .priority(Priority.IMMEDIATE)
                .into(imageView1.getImageView());

        Glide.with(GlideTestActivity.this)
                .using(new ProgressModelLoader(new ProgressHandler(GlideTestActivity.this, imageView2)))
                .load("http://c.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e950e7d5403816385343faf2b4a0.jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.img2)
                .priority(Priority.HIGH)
                .into(imageView2.getImageView());

        Glide.with(GlideTestActivity.this)
                .using(new ProgressModelLoader(new ProgressHandler(GlideTestActivity.this, imageView3)))
                .load("http://e.hiphotos.baidu.com/zhidao/pic/item/7aec54e736d12f2ed5568f4c4dc2d5628535684e.jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.img3)
                .priority(Priority.NORMAL)
                .into(imageView3.getImageView());

    }

    private static class ProgressHandler extends Handler {

        private final WeakReference<Activity> mActivity;
        private final ProgressImageView mProgressImageView;

        public ProgressHandler(Activity activity, ProgressImageView progressImageView) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
            mProgressImageView = progressImageView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        int percent = msg.arg1*100/msg.arg2;
                        mProgressImageView.setProgress(percent);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
