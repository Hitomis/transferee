package com.hitomi.transferimage.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.hitomi.tilibrary.transfer.TransferConfig;
import com.hitomi.tilibrary.transfer.Transferee;
import com.hitomi.transferimage.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;

/**
 * Created by hitomi on 2017/2/13.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int READ_EXTERNAL_STORAGE = 100;
    protected static final int WRITE_EXTERNAL_STORAGE = 101;

    protected DisplayImageOptions options;

    protected Transferee transferee;
    protected TransferConfig config;

    protected GridView gvImages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        options = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(R.mipmap.ic_empty_photo)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .build();
        transferee = Transferee.getDefault(this);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        setContentView(getContentView());
        initView();
        testTransferee();
    }

    /**
     * 保存图片到相册使用的方法
     */
    protected void saveImageFile(String imageUri) {
        String[] uriArray = imageUri.split("\\.");
        String imageName = String.format("%s.%s", String.valueOf(System.currentTimeMillis()), uriArray[uriArray.length - 1]);
        if (checkWriteStoragePermission()) {
            File rootFile = new File("/storage/emulated/0/Trasnferee/");
            boolean mkFlag = true;
            if (!rootFile.exists()) {
                mkFlag = rootFile.mkdirs();
            }
            if (mkFlag) {
                File imageFile = transferee.getImageFile(imageUri);
                boolean success = FileUtils.copyFile(imageFile, new File(rootFile, imageName));
                if (success)
                    Toast.makeText(this, "Save file success", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void testTransferee();

}
