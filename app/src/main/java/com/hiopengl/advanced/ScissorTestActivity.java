package com.hiopengl.advanced;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ScissorTestActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scissor_test);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLRenderer = new GLRenderer();
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

    private class GLRenderer implements GLSurfaceView.Renderer {

        private int mWidth;
        private int mHeight;

        public GLRenderer() {
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0F, 1.0F, 0.0F, 1.0F);
            // Clears the screen and depth buffer.
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            gl.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glEnable(GL10.GL_SCISSOR_TEST);
            // glScissor以左下角为坐标原点(0,0)，而通常情况下，坐标系以屏幕左上角为坐标原点(0,0)
            gl.glScissor(mWidth / 4, mHeight / 4, mWidth / 2, mHeight / 2);
            gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glDisable(GL10.GL_SCISSOR_TEST);
        }
    }
}