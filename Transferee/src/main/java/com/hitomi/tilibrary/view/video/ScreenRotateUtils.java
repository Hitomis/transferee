package com.hitomi.tilibrary.view.video;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;


/**
 * Created by HRR on 2020/02/22.
 */
public class ScreenRotateUtils {
    public static float orientationDirection;
    private static int DATA_X = 0;
    private static int DATA_Y = 1;
    private static int DATA_Z = 2;
    private static int ORIENTATION_UNKNOWN = -1;
    private static ScreenRotateUtils instance;
    private Context context;
    private OrientationChangeListener changeListener;

    public ScreenRotateUtils(Context context) {
        this.context = context.getApplicationContext();
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(new OrientationSensorListener(), sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static ScreenRotateUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ScreenRotateUtils(context);
        }
        return instance;
    }

    void setOrientationChangeListener(OrientationChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public interface OrientationChangeListener {
        void orientationChange(int orientation);
    }

    class OrientationSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = ORIENTATION_UNKNOWN;
            float x = -values[DATA_X];
            orientationDirection = -x;
            float y = -values[DATA_Y];
            float z = -values[DATA_Z];
            float magnitude = x * x + y * y;
            if (magnitude * 4 >= z * z) {
                float oneEightyOverPi = 57.29577957855f;
                float angle = (float) (Math.atan2(-y, x) * oneEightyOverPi);

                orientation = 90 - Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }

            /*
             * 获取手机系统的重力感应开关设置，这段代码看需求，不要就删除
             * screenchange = 1 表示开启，screenchange = 0 表示禁用
             * 要是禁用了就直接返回
             */
            // 手机系统的重力感应设置是否生效，默认无效，想要生效改成true就好了
            try {
                int isRotate = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                // 如果用户禁用掉了重力感应就直接return
                if (isRotate == 0) {
                    return;
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            // 判断是否要进行中断信息传递
            // 是否打开传输，默认打开
            changeListener.orientationChange(orientation);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

}
