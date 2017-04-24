package com.hitomi.transferimage;

import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.Transferee;

/**
 * Created by hitomi on 2017/2/13.
 */

public class BaseActivity extends AppCompatActivity {

    protected Transferee transferImage;

    @Override
    protected void onDestroy() {
        transferImage.destroy();
        super.onDestroy();
    }
}
