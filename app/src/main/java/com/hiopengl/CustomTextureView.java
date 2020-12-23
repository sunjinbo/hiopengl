package com.hiopengl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

public class CustomTextureView extends TextureView implements TextureView.SurfaceTextureListener, Runnable {

    private boolean mRunning = false;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private Rect mRect;

    public CustomTextureView(Context context) {
        super(context);
        initView();
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        mRect = new Rect(0, 0, width, height);
        mSurface = new Surface(mSurfaceTexture);
        new Thread(this).start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        mRect = new Rect(0, 0, width, height);
        mSurface = new Surface(mSurfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mRunning = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void run() {
        mRunning = true;
        while (mRunning) {
            SystemClock.sleep(333);
            Canvas canvas = mSurface.lockCanvas(mRect);
            if (canvas != null) {
                try {
                    synchronized (mSurface) {
                        onRender(canvas);
                    }
                } finally {
                    mSurface.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void onRender(Canvas canvas) {
        canvas.drawColor(Color.RED);
        // draw whatever.
    }
}
