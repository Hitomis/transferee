package com.hitomi.transferimage.progress;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by chenpengfei on 2016/11/9.
 */
public class ProgressImageView extends RelativeLayout {

    private ImageView mImageView;
    private TextView mProgressTextView;
    private int mProgress;

    public ProgressImageView(Context context) {
        super(context);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImageView = new ImageView(context);
        RelativeLayout.LayoutParams ivLp = new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        ivLp.addRule(CENTER_IN_PARENT);
        mImageView.setLayoutParams(ivLp);
        addView(mImageView);

        mProgressTextView = new TextView(context);
        mProgressTextView.setTextSize(50);
        mProgressTextView.setTextColor(Color.RED);
        RelativeLayout.LayoutParams tvLp = new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        tvLp.addRule(CENTER_IN_PARENT);
        mProgressTextView.setLayoutParams(tvLp);
        addView(mProgressTextView);
    }

    public ProgressImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        mProgressTextView.setText(mProgress + "%");
    }

    public ImageView getImageView() {
        return mImageView;
    }
}
