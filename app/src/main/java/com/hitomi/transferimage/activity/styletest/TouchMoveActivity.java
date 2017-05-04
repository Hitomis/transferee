package com.hitomi.transferimage.activity.styletest;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hitomi.glideloader.GlideImageLoader;
import com.hitomi.tilibrary.style.progress.ProgressBarIndicator;
import com.hitomi.tilibrary.transfer.TransferConfig;
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
        imageStrList.add("http://c.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e950e7d5403816385343faf2b4a0.jpg");
        imageStrList.add("http://e.hiphotos.baidu.com/zhidao/pic/item/7aec54e736d12f2ed5568f4c4dc2d5628535684e.jpg");
        imageStrList.add("http://e.hiphotos.baidu.com/zhidao/pic/item/78310a55b319ebc443ac406c8726cffc1f17166a.jpg");
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
                    .setOffscreenPageLimit(1)
                    .setProgressIndicator(new ProgressBarIndicator())
                    .create())
                    .show();
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
