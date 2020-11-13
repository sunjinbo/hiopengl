package com.hiopengl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CanvasActivity extends ActionBarActivity
        implements SurfaceHolder.Callback, Runnable {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Thread mDrawThread;
    private boolean mRunning = false;
    private Paint mPaint;
    private float[] vertexArray = new float[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surfaceview);

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        mDrawThread = new Thread(this);
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        vertexArray[0] = width / 2;
        vertexArray[1] = height / 8;

        vertexArray[2] = width / 8;
        vertexArray[3] = height - height / 8;

        vertexArray[4] = width - width / 8;
        vertexArray[5] = height - height / 8;

        vertexArray[6] = width / 2;
        vertexArray[7] = height / 8;

        vertexArray[8] = width / 8;
        vertexArray[9] = height - height / 8;

        vertexArray[10] = width - width / 8;
        vertexArray[11] = height - height / 8;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRunning = false;
        try {
            if (mDrawThread != null) {
                mDrawThread.join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mDrawThread = null;
        }
    }

    @Override
    public void run() {
        mRunning = true;
        while (mRunning) {
            try {
                Thread.sleep(333);
            }
            catch (InterruptedException ex) {}

            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                try {
                    synchronized (mSurfaceHolder) {
                        render(canvas);
                    }
                } finally {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void render(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawLines(vertexArray, mPaint);
    }
}