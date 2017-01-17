package com.hitomi.transferimage;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransferLayout transferLayout = new TransferLayout.Builder(MainActivity.this)
                        .setTransferAnima(new TransitionAnimator(MainActivity.this))
                        .setBackgroundColor(Color.BLUE)
                        .setImageResLsit(imageResList)
                        .setOriginImage(imageView)
                        .create();
                transferLayout.show();
            }
        });
    }
}
