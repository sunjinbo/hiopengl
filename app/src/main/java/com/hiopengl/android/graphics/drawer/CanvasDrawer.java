package com.hiopengl.android.graphics.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class CanvasDrawer {
    private Paint mPaint;
    private float[] vertexArray = new float[12];

    public CanvasDrawer() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setSize(int width, int height) {
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

    public void draw(Canvas canvas) {
        setSize(canvas.getWidth(), canvas.getHeight());
        canvas.drawColor(Color.WHITE);
        canvas.drawLines(vertexArray, mPaint);
    }
}
