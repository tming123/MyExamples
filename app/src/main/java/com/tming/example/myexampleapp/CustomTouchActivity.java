package com.tming.example.myexampleapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.tming.example.myexampleapp.ui.FloatWindow;
import com.tming.example.myexampleapp.util.DensityUtil;

public class CustomTouchActivity extends AppCompatActivity {
    private static final String TAG_WINDOW_REWARD_BALL = "reward_ball";
    private static final String TAG = "CustomTouchActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_touch);

        ImageView rewardBallView = new ImageView(this);
        rewardBallView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_round));

        FloatWindow.getInstance().init(this);
        FloatWindow.getInstance().setView(rewardBallView, TAG_WINDOW_REWARD_BALL);
        FloatWindow.getInstance().setWidth(DensityUtil.dp2px(44), TAG_WINDOW_REWARD_BALL);
        FloatWindow.getInstance().setHeight(DensityUtil.dp2px(44), TAG_WINDOW_REWARD_BALL);
        FloatWindow.getInstance().setEnable(true, TAG_WINDOW_REWARD_BALL);
        FloatWindow.getInstance().setMoveEnable(true);
        FloatWindow.getInstance().setBottomOffset(DensityUtil.dp2px(40));
        FloatWindow.getInstance().setOnClickListener(new FloatWindow.OnWindowViewClickListener() {
            @Override
            public void onWindowViewClick(Context windowContext) {
                Log.e(TAG, "onWindowViewClick: ");
            }
        }, TAG_WINDOW_REWARD_BALL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FloatWindow.getInstance().dismiss(TAG_WINDOW_REWARD_BALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatWindow.getInstance().show(this, TAG_WINDOW_REWARD_BALL);
    }
}
