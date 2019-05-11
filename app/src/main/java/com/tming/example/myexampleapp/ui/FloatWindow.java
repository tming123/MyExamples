package com.tming.example.myexampleapp.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.tming.example.myexampleapp.util.DensityUtil;

import java.util.HashMap;


/**
 * suspending window in app,it shows throughout the whole app
 */
public class FloatWindow implements View.OnTouchListener {
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int BORDER_OFFSET = DensityUtil.dp2px(12);
    private static final int DEFAULT_DISTANCE_FROM_BOTTOM = DensityUtil.dp2px(47);
    private static final long WELT_ANIMATION_DURATION = 150;
    private int bottomOffset;
    /**
     * several window content stored by String tag ;
     */
    private HashMap<String, WindowContent> windowContentMap;
    private WindowContent windowContent;
    private int lastTouchX;
    private int lastTouchY;
    private int screenWidth;
    private int screenHeight;
    private Activity context;
    private GestureDetector gestureDetector;
    private int navigationBarHeight;
    private boolean isMoveEnable;
    private static volatile FloatWindow INSTANCE;
    /**
     * the animation make window stick to the left or right side of screen
     */
    private ValueAnimator weltAnimator;

    public static FloatWindow getInstance() {
        if (INSTANCE == null) {
            synchronized (FloatWindow.class) {
                if (INSTANCE == null) {
                    //in case of memory leak for singleton
                    INSTANCE = new FloatWindow();
                }
            }
        }
        return INSTANCE;
    }

    public void setMoveEnable(boolean moveEnable) {
        isMoveEnable = moveEnable;
    }

    public void setBottomOffset(int bottomOffset) {
        this.bottomOffset = bottomOffset;
    }

    public FloatWindow init(Activity context) {
        this.context = context;
        prepareScreenDimension(context);
        gestureDetector = new GestureDetector(context, new GestureListener());
        return this;
    }

    private int getNavigationBarHeight(Context context) {
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    private FloatWindow() {
    }

    public void setOnClickListener(OnWindowViewClickListener listener, String tag) {
        if (windowContentMap == null) {
            return;
        }
        WindowContent windowContent = windowContentMap.get(tag);
        if (windowContent != null) {
            windowContent.clickListener = listener;
        }
    }

    public void setWidth(int width, String tag) {
        if (windowContentMap == null) {
            return;
        }
        WindowContent windowContent = windowContentMap.get(tag);
        if (windowContent != null) {
            windowContent.width = width;
        }
    }

    public void setHeight(int height, String tag) {
        if (windowContentMap == null) {
            return;
        }
        WindowContent windowContent = windowContentMap.get(tag);
        if (windowContent != null) {
            windowContent.height = height;
        }
    }

    public void setEnable(boolean enable, String tag) {
        if (windowContentMap == null) {
            return;
        }
        WindowContent windowContent = windowContentMap.get(tag);
        if (windowContent == null) {
            throw new RuntimeException("no such window view,please invoke setView() first");
        }
        windowContent.enable = enable;
    }

    public void show(Activity activity, String tag) {
        if (activity == null) {
            return;
        }
        if (activity.isFinishing()) {
            return;
        }
        if (windowContentMap == null) {
            return;
        }
        windowContent = windowContentMap.get(tag);
        if (windowContent == null) {
            return;
        }
        if (!windowContent.enable) {
            return;
        }
        if (windowContent.windowView == null) {
            return;
        }
        if (bottomOffset == 0) {
            return;
        }
        this.context = activity;
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        if (navigationBarHeight == 0) {
            navigationBarHeight = getNavigationBarHeight(activity);
        }
        prepareScreenDimension(activity);
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(activity, new GestureListener());
        }
        windowContent.layoutParams = generateLayoutParam(screenWidth, screenHeight);
        /**
         * in case of the following special situations:
         * 1. IllegalStateException :has already been added to the window manager.
         * 2. permission denied for window type 2 ：TYPE_APPLICATION is not allowed in some phone
         * 3. window token is not valid;is your activity running?
         */
        try {
            if (windowContent.windowView.getParent() == null) {
                windowManager.addView(windowContent.windowView, windowContent.layoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private WindowManager.LayoutParams generateLayoutParam(int screenWidth, int screenHeight) {
        if (context == null) {
            return new WindowManager.LayoutParams();
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = windowContent.width == 0 ? DEFAULT_WIDTH : windowContent.width;
        layoutParams.height = windowContent.height == 0 ? DEFAULT_HEIGHT : windowContent.height;
        int defaultX = screenWidth - layoutParams.width - BORDER_OFFSET;
        int defaultY = screenHeight - DEFAULT_DISTANCE_FROM_BOTTOM - layoutParams.width;
        layoutParams.x = windowContent.layoutParams == null ? defaultX : windowContent.layoutParams.x;
        layoutParams.y = windowContent.layoutParams == null ? defaultY : windowContent.layoutParams.y;
        return layoutParams;
    }

    public WindowManager.LayoutParams getLayoutParam() {
        if (windowContent != null) {
            return windowContent.layoutParams;
        }
        return null;
    }

    public Activity dismiss(String tag) {
        if (context == null) {
            return null;
        }
        if (windowContentMap == null) {
            return null;
        }
        WindowContent targetWindowContent = windowContentMap.get(tag);
        if (targetWindowContent != windowContent) {
            return context;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            //in case of "IllegalStateException :not attached to window manager."
            if (windowContent != null && windowContent.windowView != null && windowContent.windowView.getParent() != null) {
                try {
                    windowManager.removeViewImmediate(windowContent.windowView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return context;
    }

    public boolean isShowing() {
        if (windowContent == null) {
            return false;
        }
        if (windowContent.windowView == null) {
            return false;
        }
        return windowContent.windowView.getParent() != null;
    }

    /**
     * invoking this method is a must ,or window has no content to show
     *
     * @param view the content view of window
     * @param tag
     * @return
     */
    public FloatWindow setView(View view, String tag) {
        if (windowContentMap == null) {
            windowContentMap = new HashMap<>();
        }
        WindowContent windowContent = new WindowContent();
        windowContent.windowView = view;
        windowContentMap.put(tag, windowContent);
        windowContent.windowView.setOnTouchListener(this);
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //let GestureDetector take care of touch event,in order to parsing touch event into different gesture
        gestureDetector.onTouchEvent(event);
        //there is no ACTION_UP event in GestureDetector
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (windowContent != null) {
                    onActionUp(event, screenWidth, windowContent.width);
                }
                break;
            default:
                break;
        }

        return true;
    }

    private void onActionUp(MotionEvent event, int screenWidth, int width) {
        if (!isMoveEnable) {
            return;
        }
        if (windowContentMap == null || windowContent.windowView == null || windowContent.layoutParams == null) {
            return;
        }
        int upX = ((int) event.getRawX());
        int endX;
        if (upX > screenWidth / 2) {
            endX = screenWidth - width - BORDER_OFFSET;
        } else {
            endX = 0 + BORDER_OFFSET;
        }

        if (weltAnimator == null) {
            weltAnimator = ValueAnimator.ofInt(windowContent.layoutParams.x, endX);
            weltAnimator.setInterpolator(new LinearInterpolator());
            weltAnimator.setDuration(WELT_ANIMATION_DURATION);
            weltAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int x = ((int) animation.getAnimatedValue());
                    if (windowContent.layoutParams != null) {
                        windowContent.layoutParams.x = x;
                    }
                    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null && windowContent.windowView.getParent() != null) {
                        try {
                            windowManager.updateViewLayout(windowContent.windowView, windowContent.layoutParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        weltAnimator.setIntValues(windowContent.layoutParams.x, endX);
        weltAnimator.start();
    }


    private void onActionMove(MotionEvent event) {
        if (!isMoveEnable) {
            return;
        }
        int currentX = (int) event.getRawX();
        int currentY = (int) event.getRawY();
        int dx = currentX - lastTouchX;
        int dy = currentY - lastTouchY;

        windowContent.layoutParams.x += dx;
        windowContent.layoutParams.y += dy;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            int rightMost = screenWidth - windowContent.layoutParams.width - BORDER_OFFSET;
            int leftMost = 0 + BORDER_OFFSET;
            int topMost = 0 + BORDER_OFFSET;
            int bottomMost = screenHeight - windowContent.layoutParams.height - navigationBarHeight - BORDER_OFFSET;
            WindowManager.LayoutParams partnerParam = null;
            //adjust move area according to partner window
            if (partnerParam != null) {
                if (partnerParam.x < windowContent.layoutParams.x) {
                    leftMost = partnerParam.width - windowContent.layoutParams.width / 2 + BORDER_OFFSET;
                } else if (partnerParam.x > windowContent.layoutParams.x) {
                    rightMost = screenWidth - (windowContent.layoutParams.width / 2 + partnerParam.width) - BORDER_OFFSET;
                }
            }

            //make window float inside screen
            if (windowContent.layoutParams.x < leftMost) {
                windowContent.layoutParams.x = leftMost;
            }
            if (windowContent.layoutParams.x > rightMost) {
                windowContent.layoutParams.x = rightMost;
            }
            if (windowContent.layoutParams.y < topMost) {
                windowContent.layoutParams.y = topMost;
            }
            if (windowContent.layoutParams.y > bottomMost) {
                windowContent.layoutParams.y = bottomMost;
            }
            if (windowContent != null && windowContent.windowView != null && windowContent.windowView.getParent() != null) {
                try {
                    windowManager.updateViewLayout(windowContent.windowView, windowContent.layoutParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        lastTouchX = currentX;
        lastTouchY = currentY;
    }

    private RectF viewRectF;

    private void onActionDown(MotionEvent event) {
        lastTouchX = (int) event.getRawX();
        lastTouchY = (int) event.getRawY();
        viewRectF = calcViewScreenLocation();
        if (viewRectF.contains(lastTouchX, lastTouchY)) {
            Toast.makeText(context, "在范围内！！！", Toast.LENGTH_SHORT).show();
        }
    }

    private RectF calcViewScreenLocation() {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        windowContent.windowView.getLocationOnScreen(location);
        int windowViewWidth = windowContent.windowView.getWidth();
        int windowViewHeight = windowContent.windowView.getHeight();
        return new RectF(location[0], location[1], location[0] + windowViewWidth,
                location[1] + windowViewHeight);
    }

    private void prepareScreenDimension(Context activity) {
        if (screenWidth != 0 && screenHeight != 0) {
            return;
        }
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                windowManager.getDefaultDisplay().getMetrics(dm);
                screenWidth = dm.widthPixels;
                screenHeight = dm.heightPixels;
            }
        }
    }

    public interface OnWindowViewClickListener {
        void onWindowViewClick(Context windowContext);
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown(e);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (windowContent != null && windowContent.clickListener != null) {
                windowContent.clickListener.onWindowViewClick(context);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onActionMove(e2);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }

    private class WindowContent {
        /**
         * the content view of window
         */
        public View windowView;
        /**
         * the layout param of window content view
         */
        public WindowManager.LayoutParams layoutParams;
        /**
         * whether this window content is allow to show
         */
        public boolean enable = true;
        /**
         * the width of window content
         */
        public int width;
        /**
         * the height of window content
         */
        public int height;
        /**
         * the click listener of window
         */
        public OnWindowViewClickListener clickListener;

    }
}

