package com.hitomi.transferimage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.hitomi.yifangbao.tilibrary.TransferLayout;

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
                TransferLayout transferLayout = new TransferLayout(MainActivity.this);
                transferLayout.setImageUrlList(imageResList);
                transferLayout.setDisplayIndex(0);
                transferLayout.setOriginImage(imageView);
                transferLayout.show();
            }
        });
    }
}
