package com.hitomi.transferimage;

import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.Transferee;

/**
 * Created by hitomi on 2017/2/13.
 */

public class BaseActivity extends AppCompatActivity {

    protected Transferee transferee;

    @Override
    protected void onDestroy() {
        transferee.destroy();
        super.onDestroy();
    }
}
