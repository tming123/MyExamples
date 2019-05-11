package com.tming.example.myexampleapp;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class ColorFilterActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_filter);

        findViewById(R.id.btn_go_grey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = findViewById(R.id.tv_img);
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);//饱和度 0灰色 100过度彩色，50正常
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(filter);
            }
        });
    }
}
