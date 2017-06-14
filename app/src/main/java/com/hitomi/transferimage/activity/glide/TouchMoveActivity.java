package com.hitomi.transferimage.activity.glide;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class TouchMoveActivity extends BaseActivity {

    private ImageView imageView1, imageView2, imageView3;
    private List<ImageView> imageViewList;

    private List<String> imageStrList;

    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://t2.27270.com/uploads/tu/201706/9999/5a5fa496ba.jpg");
        imageStrList.add("http://t2.27270.com/uploads/tu/201706/9999/d38274f15c.jpg");
        imageStrList.add("http://t2.27270.com/uploads/tu/201705/9999/e49b76d0c3.jpg");
        imageStrList.add("http://t2.27270.com/uploads/tu/201705/9999/4c90e97526.jpg");
        imageStrList.add("http://t2.27270.com/uploads/tu/201705/9999/3a1614578a.jpg");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_touch_move;
    }

    @Override
    protected void initView() {
        imageView1 = (ImageView) findViewById(R.id.image_view1);
        imageView2 = (ImageView) findViewById(R.id.image_view2);
        imageView3 = (ImageView) findViewById(R.id.image_view3);

        TouchMoveActivity.TouchViewMotion touchViewMotion = new TouchMoveActivity.TouchViewMotion();
        imageView1.setOnTouchListener(touchViewMotion);
        imageView2.setOnTouchListener(touchViewMotion);
        imageView3.setOnTouchListener(touchViewMotion);

        TouchMoveActivity.ShowViewHDListener showViewHDListener = new TouchMoveActivity.ShowViewHDListener();
        imageView1.setOnClickListener(showViewHDListener);
        imageView2.setOnClickListener(showViewHDListener);
        imageView3.setOnClickListener(showViewHDListener);

        imageViewList = new ArrayList<>();
        imageViewList.add(imageView1);
        imageViewList.add(imageView2);
        imageViewList.add(imageView3);
    }

    @Override
    protected void testTransferee() {
        Glide.with(this)
                .load(imageStrList.get(0))
                .placeholder(R.mipmap.ic_empty_photo)
                .into(imageView1);
        Glide.with(this)
                .load(imageStrList.get(1))
                .placeholder(R.mipmap.ic_empty_photo)
                .into(imageView2);
        Glide.with(this)
                .load(imageStrList.get(2))
                .placeholder(R.mipmap.ic_empty_photo)
                .into(imageView3);
    }

    private class ShowViewHDListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            transferee.apply(TransferConfig.build()
                    .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                    .setMissPlaceHolder(R.mipmap.ic_empty_photo)
                    .setOriginImageList(imageViewList)
                    .setSourceImageList(imageStrList)
                    .setNowThumbnailIndex(imageViewList.indexOf(v))
                    .setProgressIndicator(new ProgressBarIndicator())
                    .setJustLoadHitImage(true)
                    .create())
                    .show(new Transferee.OnTransfereeStateChangeListener() {
                        @Override
                        public void onShow() {
                            Glide.with(TouchMoveActivity.this).pauseRequests();
                        }

                        @Override
                        public void onDismiss() {
                            Glide.with(TouchMoveActivity.this).resumeRequests();
                        }
                    });
        }
    }

    private class TouchViewMotion implements View.OnTouchListener {
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

                    v.setX(v.getX() + diffX);
                    v.setY(v.getY() + diffY);

                    preX = event.getRawX();
                    preY = event.getRawY();
                    break;
            }
            return false;
        }
    }
}
