package com.hitomi.tilibrary.view.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.hitomi.tilibrary.R;

public class BaseDialog extends DialogFragment{

    @Override
    public int getTheme() {
        return R.style.dialog_transparent;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null  && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.getAttributes().windowAnimations = 0; // 去掉 DialogFragment 默认动画
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(getScreenWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }
}
