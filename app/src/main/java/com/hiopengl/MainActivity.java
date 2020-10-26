package com.hiopengl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onP1Click(View view) {
        startActivity(new Intent(this, GLSurfaceViewActivity.class));
    }

    public void onP2Click(View view) {
        startActivity(new Intent(this, TextureViewActivity.class));
    }

    public void onP3Click(View view) {
        startActivity(new Intent(this, SurfaceViewActivity.class));
    }

    public void onP4Click(View view) {
        startActivity(new Intent(this, CanvasActivity.class));
    }
}