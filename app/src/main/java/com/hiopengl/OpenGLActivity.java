package com.hiopengl;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class OpenGLActivity extends AppCompatActivity {
    protected GLSurfaceView mGLSurfaceView;
    protected GLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurfaceview);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLRenderer = getRenderer();
        mGLSurfaceView.setRenderer(mGLRenderer);
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

    protected abstract GLRenderer getRenderer();

    protected abstract class GLRenderer implements GLSurfaceView.Renderer {

        protected GLRenderer() {

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            // Clears the screen and depth buffer.
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            drawFrame(gl);
        }

        abstract void drawFrame(GL10 gl);
    }
}