package com.hiopengl.android.reader;

import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.hiopengl.R;
import com.hiopengl.android.graphics.drawer.OpenGLDrawer;
import com.hiopengl.base.ActionBarActivity;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class ImageReaderActivity extends ActionBarActivity implements SurfaceHolder.Callback, Runnable {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Surface mSurface;
    private int mWidth, mHeight;
    private boolean mRunning = false;

    private ImageReader mImageReader;

    private OpenGLDrawer mOpenGLDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagereader);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
        mSurface = mImageReader.getSurface();
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
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        // 初始化EGLDisplay实例
        int[] version = new int[2];
        egl.eglInitialize(dpy, version);

        int[] configSpec = {
                EGL10.EGL_RED_SIZE,      5,
                EGL10.EGL_GREEN_SIZE,    6,
                EGL10.EGL_BLUE_SIZE,     5,
                EGL10.EGL_DEPTH_SIZE,   16,
                EGL10.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        // 选择config创建OpenGL运行环境
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];

        EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, null);
        // 创建新的surface
        EGLSurface drawSurface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
        // 将OpenGL环境设置为当前
        egl.eglMakeCurrent(dpy, drawSurface, drawSurface, context);
        // 获取当前OpenGL画布
        GL10 gl = (GL10)context.getGL();

        mOpenGLDrawer = new OpenGLDrawer();
        mOpenGLDrawer.setSize(gl, mWidth, mHeight);

        mRunning = true;
        while (mRunning) {
            SystemClock.sleep(333);
            synchronized (mSurfaceHolder) {
                render(gl);

                // 显示绘制结果到屏幕上
                egl.eglSwapBuffers(dpy, drawSurface);
            }
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, drawSurface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
    }

    public void onTakeScreenshotClick(View view) {
        synchronized (this) {

        }
    }

    private void render(GL10 gl) {
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        mOpenGLDrawer.draw(gl);
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                //处理相机预览图像 image
                image.close();
            }
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return false;
        }
    });
}
