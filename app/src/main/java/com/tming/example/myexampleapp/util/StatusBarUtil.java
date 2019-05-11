package com.tming.example.myexampleapp.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tming.example.myexampleapp.ui.SystemBarTintManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {
    public final static int TYPE_MIUI = 0;
    public final static int TYPE_FLYME = 1;
    public final static int TYPE_M = 3;//6.0

    @IntDef({TYPE_MIUI,
            TYPE_FLYME,
            TYPE_M})
    @Retention(RetentionPolicy.SOURCE)
    @interface ViewType {
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId 颜色
     */
    public static void setStatusBarColor(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(colorId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTintManager,需要先将状态栏设置为透明
            setTranslucentStatus(activity);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
            systemBarTintManager.setStatusBarTintEnabled(true);//显示状态栏
            systemBarTintManager.setStatusBarTintColor(colorId);//设置状态栏颜色
        }
    }

    /**
     * 设置状态栏透明
     */
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = window.getDecorView();
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //导航栏颜色也可以正常设置
            //window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
            //int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            //attributes.flags |= flagTranslucentNavigation;
            window.setAttributes(attributes);
        }
    }


    /**
     *  代码实现android:fitsSystemWindows
     *
     * @param activity
     */
    public static void setRootViewFitsSystemWindows(Activity activity, boolean fitSystemWindows) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup winContent = activity.findViewById(android.R.id.content);
            if (null != winContent && winContent.getChildCount() > 0) {
                ViewGroup rootView = (ViewGroup) winContent.getChildAt(0);
                if (rootView != null) {
                    rootView.setFitsSystemWindows(fitSystemWindows);
                }
            }
        }

    }

    /**
     * 设置状态栏模式
     * @param activity
     * @param isTextDark 文字、图标是否为黑色 （false为默认的白色）
     * @param colorId 状态栏颜色
     * @return
     */
    public static boolean setStatusBarDarkTheme(Activity activity, boolean isTextDark, int colorId) {
        boolean result = false;
        if(!isTextDark) {
            //文字、图标颜色不变，只修改状态栏颜色
            setStatusBarColor(activity, colorId);
            result = true;
        } else {
            //修改状态栏颜色和文字图标颜色
            setStatusBarColor(activity, colorId);
            //4.4以上才可以改文字图标颜色
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(OSUtils.isMIUI()) {
                    //小米MIUI系统
                    result = setMiuiUI(activity, isTextDark);
                } else if(OSUtils.isFlyme()) {
                    //魅族flyme系统
                    result = setFlymeUI(activity, isTextDark);
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //6.0以上，调用系统方法
                    result = setCommonUI(activity, isTextDark);
                } else {
                    //4.4以上6.0以下的其他系统，暂时没有修改状态栏的文字图标颜色的方法，有可以加上
                }
            }
        }
        return result;
    }

    //设置6.0 状态栏深色浅色切换
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean setCommonUI(Activity activity, boolean dark) {
        return setCommonUI(activity, "Common", dark);
    }

    //设置6.0 状态栏深色浅色切换
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean setCommonUI(Activity activity, String platfrom, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                //6.0以上，调用系统方法
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    //设置Flyme 状态栏深色浅色切换
    public static boolean setFlymeUI(Activity activity, boolean dark) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            //执行成功也走一遍常规的代码
            setCommonUI(activity, "Flyme#Common", dark);
            return true;
        } catch (Exception e) {
            // 异常后先以默认配置方式执行一遍
            return setCommonUI(activity, "Flyme#Common", dark);
        }
    }

    //设置MIUI 状态栏深色浅色切换
    public static boolean setMiuiUI(Activity activity, boolean dark) {
        try {
            Window window = activity.getWindow();
            Class<?> clazz = activity.getWindow().getClass();
            @SuppressLint("PrivateApi") Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getDeclaredMethod("setExtraFlags", int.class, int.class);
            extraFlagField.setAccessible(true);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                if (dark) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View
                            .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                            .SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View
                            .SYSTEM_UI_FLAG_VISIBLE);
                }
            }
            //执行成功也走一遍常规的代码
            setCommonUI(activity, "Miui#Common", dark);
            return true;
        } catch (Exception e) {
            return setCommonUI(activity, "Miui#Common", dark);
        }
    }
    //获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        if (null != context) {
            int resourceId = context.getResources().getIdentifier(
                    "status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static int getSystemTintPadding(Context context) {
        int result = 0;
        //todo 5.0以下不适配状态栏
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return result;
        } else {
            return getStatusBarHeight(context);
        }
    }

    public static void setStatusBarDarkIcon(Activity activity, boolean dark) {
        if (dark) {
            //设置状态栏文字颜色及图标为深色
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            //设置状态栏文字颜色及图标为浅色
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}