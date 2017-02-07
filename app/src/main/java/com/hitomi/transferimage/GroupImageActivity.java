package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.tilibrary.TransferImage;
import com.hitomi.tilibrary.loader.glide.GlideImageLoader;
import com.hitomi.tilibrary.style.anim.TransitionAnimator;
import com.hitomi.tilibrary.style.index.IndexCircleIndicator;
import com.hitomi.tilibrary.style.progress.ProgressPieIndicator;

import java.util.ArrayList;
import java.util.List;

public class GroupImageActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2, imageView3;
    private List<String> imageStrList;
    private List<ImageView> imageViewList;
    {
        imageStrList = new ArrayList<>();
        imageStrList.add("http://img5.niutuku.com/phone/1212/3752/3752-niutuku.com-22310.jpg");
        imageStrList.add("http://c.hiphotos.baidu.com/zhidao/pic/item/b7003af33a87e950e7d5403816385343faf2b4a0.jpg");
        imageStrList.add("http://e.hiphotos.baidu.com/zhidao/pic/item/7aec54e736d12f2ed5568f4c4dc2d5628535684e.jpg");
        imageStrList.add("http://e.hiphotos.baidu.com/zhidao/pic/item/78310a55b319ebc443ac406c8726cffc1f17166a.jpg");
        imageStrList.add("http://img2.niutuku.com/desk/anime/1948/1948-13519.jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_image);

        imageView1 = (ImageView) findViewById(R.id.image_view1);
        imageView2 = (ImageView) findViewById(R.id.image_view2);
        imageView3 = (ImageView) findViewById(R.id.image_view3);

        GroupImageActivity.TouchViewMotion touchViewMotion = new GroupImageActivity.TouchViewMotion();
        imageView1.setOnTouchListener(touchViewMotion);
        imageView2.setOnTouchListener(touchViewMotion);
        imageView3.setOnTouchListener(touchViewMotion);


        GroupImageActivity.ShowViewHDListener showViewHDListener = new GroupImageActivity.ShowViewHDListener();
        imageView1.setOnClickListener(showViewHDListener);
        imageView2.setOnClickListener(showViewHDListener);
        imageView3.setOnClickListener(showViewHDListener);

        imageViewList = new ArrayList<>();
        imageViewList.add(imageView1);
        imageViewList.add(imageView2);
        imageViewList.add(imageView3);
    }

    private class ShowViewHDListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TransferImage transferLayout = new TransferImage.Builder(GroupImageActivity.this)
                    .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                    .setTransferAnima(new TransitionAnimator())
                    .setProgressIndicator(new ProgressPieIndicator())
                    .setIndexIndicator(new IndexCircleIndicator())
                    .setBackgroundColor(Color.BLACK)
                    .setImageStrList(imageStrList)
                    .setOriginImageList(imageViewList)
                    .setOriginIndex(imageViewList.indexOf(v))
                    .setOffscreenPageLimit(1)
                    .create();
            transferLayout.show();
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
