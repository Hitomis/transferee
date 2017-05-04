package com.hitomi.transferimage.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hitomi.tilibrary.transfer.Transferee;

/**
 * Created by hitomi on 2017/2/13.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected Transferee transferee;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferee = Transferee.getDefault(this);
        setContentView(getContentView());
        initView();
        testTransferee();
    }

    protected abstract int getContentView();
    protected abstract void initView();
    protected abstract void testTransferee();

    @Override
    protected void onDestroy() {
        transferee.destroy();
        super.onDestroy();
    }
}
