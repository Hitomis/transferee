package com.hitomi.transferimage;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 
 * @author 赵帆
 * 尺寸工具类
 *
 */
public class DensityUtil {	
	
	 /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2Dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }
    
    /**
     * 将sp值转换为px值，保证文字大小不变
     * 
     * @param spValue
     * @param spValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */ 
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (spValue * fontScale + 0.5f); 
    } 
    
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2Sp(Context context, float pxValue) {
    	final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (pxValue / fontScale + 0.5f);
    }
    
    /**
     * 根据上下文重置DisplayMetrics
     */
    public static DisplayMetrics dueDisplayMetrics(Context context){
    	WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    	DisplayMetrics dm=new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }
    
    /**
     * 获取屏幕宽度
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context){
    	DisplayMetrics dm=dueDisplayMetrics(context);
    	return dm.widthPixels;
    }
    
    /**
     * 获取屏幕高度
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context){
    	DisplayMetrics dm=dueDisplayMetrics(context);
    	return dm.heightPixels;
    }
}
