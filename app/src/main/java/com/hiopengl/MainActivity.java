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

    public void onGLSurfaceViewClick(View view) {
        startActivity(new Intent(this, GLSurfaceViewActivity.class));
    }

    public void onTextureViewClick(View view) {
        startActivity(new Intent(this, TextureViewActivity.class));
    }

    public void onSurfaceViewClick(View view) {
        startActivity(new Intent(this, SurfaceViewActivity.class));
    }

    public void onCanvasClick(View view) {
        startActivity(new Intent(this, CanvasActivity.class));
    }

    public void onGeometricClick(View view) {
        startActivity(new Intent(this, GeometricActivity.class));
    }

    public void onPolyhedronClick(View view) {
        startActivity(new Intent(this, PolyhedronActivity.class));
    }

    public void onGLSLClick(View view) {

    }

    public void onModelViewClick(View view) {

    }

    public void onProjectionClick(View view) {

    }

    public void onViewportClick(View view) {

    }

    public void onLightingClick(View view) {

    }

    public void onTextureClick(View view) {

    }

    public void onParticleClick(View view) {

    }
}