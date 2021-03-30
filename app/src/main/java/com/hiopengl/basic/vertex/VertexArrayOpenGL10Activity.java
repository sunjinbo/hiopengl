package com.hiopengl.basic.vertex;

import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VertexArrayOpenGL10Activity extends VertexActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initVertex();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mPositionFloatBuffer);

        gl.glRotatef(0.5f, 0.5f, 0.5f, 0.0f);

        // Set flat color
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

        // Smooth color
        if (mColorFloatBuffer != null ) {
            // Enable the color array buffer to be used during rendering.
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            // Point out the where the color buffer is.
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorFloatBuffer);
        }

        gl.glDrawArrays(GL10.GL_LINES, 0, 24);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}