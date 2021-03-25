package com.hiopengl.android.graphics.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.hiopengl.android.graphics.drawer.OpenGLDrawer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private OpenGLDrawer mDrawer;

    public OpenGLGLSurfaceView(Context context) {
        super(context);
        mDrawer = new OpenGLDrawer();
        setRenderer(this);
    }

    public OpenGLGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawer = new OpenGLDrawer();
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // pass through
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDrawer.setSize(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mDrawer.draw(gl);
    }
}
