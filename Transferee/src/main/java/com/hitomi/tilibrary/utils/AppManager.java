package com.hitomi.tilibrary.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 监听前后台切换以及屏幕熄灭、点亮
 * Created by Vans Z on 2020/5/23.
 */
public class AppManager implements Application.ActivityLifecycleCallbacks {
    private int foregroundCount;
    private final Set<OnAppStateChangeListener> listenerSet = new HashSet<>();

    private static class SingletonHolder {
        final static AppManager instance = new AppManager();
    }

    public static AppManager getInstance() {
        return SingletonHolder.instance;
    }

    public void init(Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    public void register(OnAppStateChangeListener listener) {
        listenerSet.add(listener);
    }

    public void unregister(OnAppStateChangeListener listener) {
        listenerSet.remove(listener);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        foregroundCount++;
        if (foregroundCount == 1) { // 从后台回到了前台
            for (OnAppStateChangeListener listener : listenerSet) {
                listener.onForeground();
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        foregroundCount--;
        if (foregroundCount == 0) { // 从前台回到了后台
            for (OnAppStateChangeListener listener : listenerSet) {
                listener.onBackground();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public interface OnAppStateChangeListener {
        void onForeground();

        void onBackground();
    }

    public void clear() {
        listenerSet.clear();
    }
}
