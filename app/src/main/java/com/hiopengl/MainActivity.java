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

    public void onSurfaceTextureClick(View view) {
        startActivity(new Intent(this, SurfaceTextureActivity.class));
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
        startActivity(new Intent(this, GLSLActivity.class));
    }

    public void onViewModelClick(View view) {
        startActivity(new Intent(this, ViewModelActivity.class));
    }

    public void onProjectionClick(View view) {
        startActivity(new Intent(this, ProjectionActivity.class));
    }

    public void onViewportClick(View view) {
        startActivity(new Intent(this, ViewportActivity.class));
    }

    public void onFaceCullingClick(View view) {
        startActivity(new Intent(this, FaceCullingActivity.class));
    }

    public void onVertexClick(View view) {
        startActivity(new Intent(this, VertexActivity.class));
    }

    public void onPhongLightingClick(View view) {
        startActivity(new Intent(this, PhongLightingActivity.class));
    }

    public void onLightingSourceClick(View view) {
        startActivity(new Intent(this, LightingSourceActivity.class));
    }

    public void onMaterialClick(View view) {
        startActivity(new Intent(this, MaterialActivity.class));
    }

    public void onTextureClick(View view) {
        startActivity(new Intent(this, TextureActivity.class));
    }

    public void onMeshClick(View view) {
        startActivity(new Intent(this, MeshActivity.class));
    }

    public void onFrameBufferClick(View view) {
        startActivity(new Intent(this, FrameBufferActivity.class));
    }

    public void onDepthTestClick(View view) {
        startActivity(new Intent(this, DepthTestActivity.class));
    }

    public void onParticleClick(View view) {
        startActivity(new Intent(this, ParticleActivity.class));
    }
}