package com.hitomi.transferimage;

import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.TransferImage;

/**
 * Created by hitomi on 2017/2/13.
 */

public class BaseActivity extends AppCompatActivity {

    protected TransferImage transferLayout;

    @Override
    public void onBackPressed() {

        if (transferLayout != null && transferLayout.isShown()) {
            transferLayout.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
