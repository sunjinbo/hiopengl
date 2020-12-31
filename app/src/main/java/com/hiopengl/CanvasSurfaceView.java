package com.hiopengl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CanvasSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private boolean mRunning = false;
    private SurfaceHolder mSurfaceHolder;
    private CanvasDrawer mDrawer;

    public CanvasSurfaceView(Context context) {
        super(context);
        initView();
    }

    public CanvasSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CanvasSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mDrawer = new CanvasDrawer();
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
                        mDrawer.draw(canvas);
                    }
                } finally {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
