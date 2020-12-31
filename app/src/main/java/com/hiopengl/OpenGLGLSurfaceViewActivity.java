package com.hiopengl;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class OpenGLGLSurfaceViewActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_glsurfaceview);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }
}
