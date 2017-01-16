package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.TransferLayout;
import com.hitomi.yifangbao.tilibrary.style.anim.TransitionAnimator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private List<Integer> imageResList;

    {
        imageResList = new ArrayList<>();
        imageResList.add(R.drawable.img1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                TransferLayout transferLayout = new TransferLayout.Builder(MainActivity.this)
                        .setTransferAnima(new TransitionAnimator())
                        .setBackgroundColor(Color.BLUE)
                        .setImageResLsit(imageResList)
                        .setOriginImage(imageView)
                        .create();
                transferLayout.show();
            }
        });
    }
}
