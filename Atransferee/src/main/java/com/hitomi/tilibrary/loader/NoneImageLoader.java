package com.hitomi.tilibrary.loader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by Hitomis on 2017/5/3 0003.
 */

public class NoneImageLoader implements ImageLoader {

    @Override
    public void showSourceImage(String srcUrl, ImageView imageView,
                                Drawable placeholder, SourceCallback sourceCallback) {}

    @Override
    public void loadThumbnailAsync(String thumbUrl,
                                   ImageView imageView, ThumbnailCallback callback) {}

}
