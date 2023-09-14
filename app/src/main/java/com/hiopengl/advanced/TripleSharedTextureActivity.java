package com.hiopengl.advanced;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hiopengl.R;
import com.hiopengl.advanced.tasks.RenderGL;
import com.hiopengl.advanced.tasks.VideoSinkGL;
import com.hiopengl.base.ActionBarActivity;

import javax.microedition.khronos.egl.EGLContext;

public class TripleSharedTextureActivity extends ActionBarActivity
        implements SurfaceHolder.Callback, VideoSinkGL.Callback {

    private SurfaceView mSurfaceView;

    private VideoSinkGL mVideoSinkGL;
    private RenderGL mRenderGL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triple_shared_texture);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mRenderGL = new RenderGL(this, surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        mRenderGL.setRenderSize(w, h);
        mVideoSinkGL = new VideoSinkGL(this, w, h, this);
        mVideoSinkGL.startGL();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void initEgl(EGLContext eglContext, int textureId) {
        mRenderGL.setSharedContext(eglContext, textureId);
        mRenderGL.startGL();
    }
}
