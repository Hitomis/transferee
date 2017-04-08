package com.hitomi.transferimage;

import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.Transferee;
import com.jaeger.library.StatusBarUtil;

/**
 * Created by hitomi on 2017/2/13.
 */

public class BaseActivity extends AppCompatActivity {

    protected Transferee transferee;

    protected void updateStatusBar(){
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary), 0);
    }

    @Override
    protected void onDestroy() {
        transferee.destroy();
        super.onDestroy();
    }
}
