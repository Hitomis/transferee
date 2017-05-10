package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 图片加载器空实现，防止 NullPointer
 * Created by Hitomis on 2017/5/3 0003.
 * <p>
 * email: 196425254@qq.com
 */
public class NoneImageLoader implements ImageLoader {

    @Override
    public void showSourceImage(String srcUrl, ImageView imageView,
                                Drawable placeholder, SourceCallback sourceCallback) {
    }

    @Override
    public void loadThumbnailAsync(String thumbUrl,
                                   ImageView imageView, ThumbnailCallback callback) {
    }

    @Override
    public boolean isLoaded(String url) {
        return false;
    }

    @Override
    public void clearCache() {
    }

}
