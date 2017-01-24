package com.hitomi.transferimage;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;

public class GlideTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_test);

        /**
         * http://www.jianshu.com/p/548031c62257
         */
//        Glide.with(this).using(null, null).load("").as(null).placeholder().load().into()

    }

    public void popDialog() {
//        Dialog dialog

    }

}
