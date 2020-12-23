package com.hiopengl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private boolean mRunning = false;
    private SurfaceHolder mSurfaceHolder;

    public CustomSurfaceView(Context context) {
        super(context);
        initView();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRunning = false;
    }

    @Override
    public void run() {
        mRunning = true;
        while (mRunning) {
            SystemClock.sleep(333);
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                try {
                    synchronized (mSurfaceHolder) {
                        onRender(canvas);
                    }
                } finally {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void onRender(Canvas canvas) {
        canvas.drawColor(Color.RED);
        // draw whatever.
    }
}
