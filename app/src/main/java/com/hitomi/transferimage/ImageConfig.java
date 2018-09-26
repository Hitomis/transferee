package com.hitomi.transferimage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hitomi on 2018/9/26.
 */

public class ImageConfig {

    public static final String THUM_URL = "http://static.fdc.com.cn/avatar/sns/1486263782969.png@233w_160h_20q";
    public static final String SOURCE_URL = "http://static.fdc.com.cn/avatar/sns/1486263782969.png";
    public static final String WEB_URL = "http://img2.woyaogexing.com/2018/01/25/f5d815584c61d376!500x500.jpg";

    public static List<String> getThumbnailPicUrlList() {
        List<String> thumbnailImageList = new ArrayList<>();
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png@233w_160h_20q");
        return thumbnailImageList;
    }

    public static List<String> getSourcePicUrlList() {
        List<String> sourceImageList = new ArrayList<>();
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png");
        return sourceImageList;
    }

    public static List<String> getWebPicUrlList() {
        List<String> sourceImageList = new ArrayList<>();
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/25/f5d815584c61d376!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/25/f39e625574dd6169!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/25/4771243daf1c4e38!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/25/991349aa8c98c502!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/25/090cf5fd769351a7!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/02/02/be4ffaa3df84a9fd!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/16/ebb71389722b2bc4!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/16/56adca0f49dde198!500x500.jpg");
        sourceImageList.add("http://img2.woyaogexing.com/2018/01/16/78b37fd847279e8c!500x500.jpg");
        return sourceImageList;
    }

    /**
     * 使用ContentProvider读取SD卡最近图片
     *
     * @param maxCount 读取的最大张数
     * @return
     */
    public static List<String> getLatestPhotoPaths(Context context, int maxCount) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;

        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpg和png的图片,按最新修改排序
        Cursor cursor = mContentResolver.query(mImageUri, new String[]{key_DATA},
                key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
                new String[]{"image/jpg", "image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED);

        List<String> latestImagePaths = null;
        if (cursor != null) {
            //从最新的图片开始读取.
            //当cursor中没有数据时，cursor.moveToLast()将返回false
            if (cursor.moveToLast()) {
                latestImagePaths = new ArrayList<String>();

                while (true) {
                    // 获取图片的路径
                    String path = "file:/" + cursor.getString(0);
                    if (!latestImagePaths.contains(path))
                        latestImagePaths.add(path);

                    if (latestImagePaths.size() >= maxCount || !cursor.moveToPrevious()) {
                        break;
                    }
                }
            }
            cursor.close();
        }

        return latestImagePaths;
    }
}
