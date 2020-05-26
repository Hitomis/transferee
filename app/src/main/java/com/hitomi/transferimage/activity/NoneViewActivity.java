package com.hitomi.transferimage.activity;

import android.view.View;

import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.transferimage.SourceConfig;
import com.hitomi.transferimage.R;
import com.vansz.glideimageloader.GlideImageLoader;

/**
 * 不绑定任何 view ,直接显示
 * Created by Vans Z on 2020/4/16.
 */
public class NoneViewActivity extends BaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.activity_none_view;
    }

    @Override
    protected void initView() {
        findViewById(R.id.btn_none).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferee.apply(TransferConfig.build()
                        .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                        .setSourceUrlList(SourceConfig.getWebPicUrlList())
                        .create()
                ).show();
            }
        });
    }

    @Override
    protected void testTransferee() {
    }
}
