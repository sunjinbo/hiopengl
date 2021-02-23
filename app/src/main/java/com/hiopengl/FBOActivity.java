package com.hiopengl;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.hiopengl.glsl.TrianglesProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FBOActivity extends ActionBarActivity {
    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scissor_test);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
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

        private TrianglesProgram mProgram;
        private int mFBO;

        public GLRenderer() {
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0F, 1.0F, 0.0F, 1.0F);
            // Clears the screen and depth buffer.
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            mProgram = new TrianglesProgram(FBOActivity.this);

            int[] buffer = new int[1];
            GLES30.glGenFramebuffers(buffer.length, buffer, 0);
            mFBO = buffer[0];


        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;
            GLES30.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);

            mProgram.draw();

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            mProgram.draw();
        }
    }
}
