package com.tming.example.myexampleapp.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 单位换算工具类
 */
public final class DensityUtil {

    private DensityUtil() {
        /** cannot be instantiated **/
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * dp转px
     */
    public static int dp2px(float dpVal) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpVal * scale + 0.5f);
    }

    /**
     * sp转px
     */
    public static int sp2px(float spVal) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spVal * fontScale + 0.5f);
    }

    /**
     * px转dp
     */
    public static float px2dp(float pxVal) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     */
    public static float px2sp(float pxVal) {
        return (pxVal / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    private static DisplayMetrics metrics;

    public static DisplayMetrics getDisplayMetrics(Context context) {
        if (metrics != null) {
            return metrics;
        }
        metrics = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (metrics != null && windowMgr != null) {
            windowMgr.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }
}
