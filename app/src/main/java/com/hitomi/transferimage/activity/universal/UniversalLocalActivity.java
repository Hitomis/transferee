package com.hitomi.transferimage.activity.universal;

import android.widget.GridView;

import com.hitomi.transferimage.R;
import com.hitomi.transferimage.activity.BaseActivity;

/**
 * Created by hitomi on 2017/6/14.
 */

public class UniversalLocalActivity extends BaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.activity_grid_view;
    }

    @Override
    protected void initView() {
        gvImages = (GridView) findViewById(R.id.gv_images);
    }

    @Override
    protected void testTransferee() {

    }

}
