package com.hiopengl.android.reader;

import android.opengl.Matrix;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.opengl.EGL14;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.hiopengl.R;
import com.hiopengl.advanced.model.Torus;
import com.hiopengl.android.graphics.drawer.OpenGLDrawer;
import com.hiopengl.base.ActionBarActivity;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

public class PBOActivity extends ActionBarActivity implements SurfaceHolder.Callback, Runnable {
    private static final int MSG_SHOW_SCREENSHOT = 0;
    private static final int MSG_HIDE_SCREENSHOT = 1;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int mWidth, mHeight;
    private boolean mRunning = false;

    private EGLSurface mDrawSurface;
    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;

    private FrameLayout mFrameView;
    private ImageView mScreenshotView;

    private volatile boolean mIsCapturing = false;

    private Torus mTorus;
    private float[] mMVPMatrix = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_pbo);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mFrameView = findViewById(R.id.frame);
        mScreenshotView = findViewById(R.id.screenshot);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @SuppressLint("WrongConstant")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRunning = false;
    }

    @Override
    public void run() {
        // 创建一个EGL实例
        EGL10 egl = (EGL10) EGLContext.getEGL();
        // 传教一个EGLDisplay实例
        mEGLDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        // 初始化EGLDisplay实例
        int[] version = new int[2];
        egl.eglInitialize(mEGLDisplay, version);

        int[] configSpec = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        // 选择config创建OpenGL运行环境
        egl.eglChooseConfig(mEGLDisplay, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];

        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,// 0x3098
                EGL14.EGL_NONE
        };

        mEGLContext = egl.eglCreateContext(mEGLDisplay, config,
                EGL10.EGL_NO_CONTEXT, ctxAttr);
        // 创建新的surface
        mDrawSurface = egl.eglCreateWindowSurface(mEGLDisplay, config, mSurfaceHolder, null);

        // 将OpenGL环境设置为当前
        egl.eglMakeCurrent(mEGLDisplay, mDrawSurface, mDrawSurface, mEGLContext);
        // 获取当前OpenGL画布
        GL10 gl = (GL10)mEGLContext.getGL();

        mTorus = new Torus(PBOActivity.this, 0.4f, 0.2f, 10, 10);
        Matrix.setIdentityM(mMVPMatrix, 0);

        mRunning = true;
        while (mRunning) {
            synchronized (mSurfaceHolder) {
                render(gl);

                // 显示绘制结果到屏幕上
                egl.eglSwapBuffers(mEGLDisplay, mDrawSurface);
            }
            SystemClock.sleep(333);
        }

        egl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(mEGLDisplay, mDrawSurface);
        egl.eglDestroyContext(mEGLDisplay, mEGLContext);
        egl.eglTerminate(mEGLDisplay);
    }

    public void onTakeScreenshotClick(View view) {
        synchronized (this) {
            mIsCapturing = true;
        }
    }

    private void render(GL10 gl) {
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        mTorus.draw(gl, mMVPMatrix);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SHOW_SCREENSHOT:
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