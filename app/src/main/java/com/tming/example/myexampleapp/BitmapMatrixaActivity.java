package com.tming.example.myexampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.tming.example.myexampleapp.ui.CommonImgMatrixView;

public class BitmapMatrixaActivity extends AppCompatActivity {
    private CommonImgMatrixView effectView;
    private Button btnScale, btnRotate, btnTranslate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap_matrix);

        effectView = findViewById(R.id.img_bitmap);
        btnScale = findViewById(R.id.btn_scale);
        btnRotate = findViewById(R.id.btn_rotate);
        btnTranslate = findViewById(R.id.btn_ranslate);

        btnScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}
