package com.hitomi.transferimage;

import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.TransferImage;

/**
 * Created by hitomi on 2017/2/13.
 */

public class BaseActivity extends AppCompatActivity {

    protected TransferImage transferImage;

    @Override
    public void onBackPressed() {

        if (transferImage != null && transferImage.isShown()) {
            transferImage.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
