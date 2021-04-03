package com.hiopengl.android.reader;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.hiopengl.R;
import com.hiopengl.basic.vertex.VertexActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLReadPixelsActivity extends VertexActivity {

    private static final int MSG_SHOW_SCREENSHOT = 0;
    private static final int MSG_HIDE_SCREENSHOT = 1;

    private FrameLayout mFrameView;
    private ImageView mScreenshotView;

    private int mWidth, mHeight;
    private ByteBuffer mPixelBuf;
    private volatile boolean mTakeScreenshot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setRenderer(this);
        mFrameView = findViewById(R.id.frame);
        mScreenshotView = findViewById(R.id.screenshot);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_glreadpixels;
    }

    public void onTakeScreenshotClick(View view) {
        synchronized (this) {
            mTakeScreenshot = true;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initVertexBuffer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;
        mPixelBuf = ByteBuffer.allocateDirect(width * height * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
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

        synchronized (this) {
            if (mTakeScreenshot) {
                mTakeScreenshot = false;

                mPixelBuf.position(0);
                gl.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, mPixelBuf);
                mPixelBuf.rewind();
                Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(mPixelBuf);
                Message message = Message.obtain();
                message.what = MSG_SHOW_SCREENSHOT;
                message.obj = bmp;
                mHandler.sendMessage(message);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_SHOW_SCREENSHOT:
                mScreenshotView.setImageBitmap((Bitmap) msg.obj);
                mFrameView.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_SCREENSHOT, 3000);
                break;

            case MSG_HIDE_SCREENSHOT:
                mFrameView.setVisibility(View.GONE);
                break;

            default:
                break;
        }

        return false;
        }
    });
}
