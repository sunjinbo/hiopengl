package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.os.Bundle;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.hiopengl.R;

// Sync with the recording.
public abstract class SyncRecorderActivity extends RecorderActivity
        implements SurfaceHolder.Callback {

    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mSurfaceHolder;
    protected Button mRecordButton;
    protected boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        mRecordButton = findViewById(R.id.recorder);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    mRenderHandler.stopRecord();
                    mRecordButton.setText("START RECORD");
                } else {
                    mRenderHandler.startRecord();
                    mRecordButton.setText("STOP RECORD");
                }
            }
        });

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startRecorderLooper();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        mWidth = width;
        mHeight = height;
        waitUntilLooperReady();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRecorderLooper();
    }

    @Override
    EGLSurface createSurface() {
        int[] surfaceAttribs = { EGL14.EGL_NONE };
        return EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurfaceHolder,
                surfaceAttribs, 0);
    }
}