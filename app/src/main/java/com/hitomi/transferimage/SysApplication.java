package com.hitomi.transferimage;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by Vans Z on 2017/4/19.
 */

public class SysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
