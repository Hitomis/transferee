package com.hitomi.tilibrary.loader;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.hitomi.tilibrary.utils.FileUtils;
import com.hitomi.tilibrary.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.zibin.luban.Luban;

import static com.hitomi.tilibrary.utils.ImageUtils.TYPE_GIF;

/**
 * Created by Vans Z on 2020/6/2.
 */
public class ImageProcessor {
    private static final int THREAD_COUNT = 32;
    private ExecutorService execService;
    private ImageHandler handler;
    private static Map<String, ImageProcessCallback> callbackMap;

    private ImageProcessor() {
        init();
    }

    private static class SingletonHolder {
        private final static ImageProcessor instance = new ImageProcessor();
    }

    public static ImageProcessor getInstance() {
        return ImageProcessor.SingletonHolder.instance;
    }

    private void init() {
        execService = Executors.newFixedThreadPool(THREAD_COUNT);
        handler = new ImageHandler();
        callbackMap = new HashMap<>();
    }

    public void process(Context context, final String key, final File sourceFile, final File savedDir,
                        final Point displaySize, @NonNull ImageProcessCallback callback) {
        final File targetFile = new File(savedDir, getFileName(key));
        if (!isNeedProcess(sourceFile, displaySize.x, displaySize.y)
                || ImageUtils.getImageType(sourceFile) == TYPE_GIF) {
            // 不需要处理的图片和 gif 图，直接复制到指定文件夹
            callbackMap.put(key, callback);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUtils.copyFile(sourceFile, targetFile);
                    handler.sendMessage(handler.obtainMessage(200, new Pair<>(key, targetFile)));
                }
            }).start();
        } else if (FileUtils.isFileExists(targetFile)) { // 处理后的图片已经存在
            callback.onSuccess(targetFile);
        } else {
            callbackMap.put(key, callback);
            execService.execute(new ImageTask(context, key, sourceFile, savedDir));
        }
    }

    /**
     * 检查图片是否需要采样率压缩和图片旋转处理
     */
    private boolean isNeedProcess(final File source, final int maxWidth, final int maxHeight) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), newOpts);
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int orientation = getImageOrientation(source.getAbsolutePath());
        boolean needAdjustOrientation = orientation != ExifInterface.ORIENTATION_UNDEFINED
                && orientation != ExifInterface.ORIENTATION_NORMAL;
        return needAdjustOrientation || w > maxWidth || h > maxHeight;
    }

    private String getFileName(String imageUrl) {
        String[] nameArray = imageUrl.split("/");
        return nameArray[nameArray.length - 1];
    }

    private int getImageOrientation(String sourcePath) {
        try {
            ExifInterface exifInfo = new ExifInterface(sourcePath);
            return exifInfo.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );
        } catch (Exception ignored) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    private void adjustOrientation(File source, File target) {
        String sourcePath = source.getAbsolutePath();
        int orientation = getImageOrientation(sourcePath);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90f);
                break;
            default:
                FileUtils.copyFile(source, target);
                return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(sourcePath);
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true
        );
        bitmap.recycle();
        FileUtils.save(rotatedBitmap, target);
    }

    private static class ImageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                Pair<String, File> data = (Pair<String, File>) msg.obj;
                ImageProcessCallback callback = callbackMap.get(data.first);
                if (callback != null) callback.onSuccess(data.second);
                callbackMap.remove(data.first);
            } else {
                ImageProcessCallback callback = callbackMap.get(msg.obj.toString());
                if (callback != null) callback.onFailure();
                callbackMap.remove(msg.obj.toString());
            }
            super.handleMessage(msg);
        }
    }

    private class ImageTask implements Runnable {
        private Context context;
        private String key;
        private File originFile;
        private File savedDir;


        public ImageTask(Context context, String key, File originFile, File savedDir) {
            this.context = context;
            this.key = key;
            this.originFile = originFile;
            this.savedDir = savedDir;
        }

        @Override
        public void run() {
            try {
                // 先压缩
                List<File> files = Luban.with(context)
                        .load(originFile)
                        .ignoreBy(150)
                        .setTargetDir(savedDir.getAbsolutePath())
                        .get();
                File targetFile = new File(savedDir, getFileName(key));
                File compressFile;
                if (files != null && !files.isEmpty()) {
                    compressFile = files.get(0);
                } else {
                    compressFile = originFile;
                }
                // 再调整图片方向
                adjustOrientation(compressFile, targetFile);

                if (!compressFile.getAbsolutePath().equals(originFile.getAbsolutePath())) {
                    // 如果压缩图片不是原图片，那么删除压缩图片
                    FileUtils.delete(compressFile);
                }
                handler.sendMessage(handler.obtainMessage(200, new Pair<>(key, targetFile)));
            } catch (IOException ignored) {
                handler.sendMessage(handler.obtainMessage(0, key));
            }
        }
    }

    public interface ImageProcessCallback {
        void onSuccess(File file);

        void onFailure();
    }
}
