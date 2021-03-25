package com.hiopengl.android.reader;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.hiopengl.android.graphics.drawer.OpenGLDrawer;
import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLReadPixelsActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    private static final int MSG_SHOW_SCREENSHOT = 0;
    private static final int MSG_HIDE_SCREENSHOT = 1;

    private FrameLayout mFrameView;
    private ImageView mScreenshotView;
    private GLSurfaceView mGLSurfaceView;
    private OpenGLDrawer mDrawer;
    private int mWidth, mHeight;
    private ByteBuffer mPixelBuf;
    private volatile boolean mTakeScreenshot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glreadpixels);
        mFrameView = findViewById(R.id.frame);
        mScreenshotView = findViewById(R.id.screenshot);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setRenderer(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    public void onTakeScreenshotClick(View view) {
        mTakeScreenshot = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
        mDrawer = new OpenGLDrawer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        mDrawer.setSize(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mDrawer.draw(gl);

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
