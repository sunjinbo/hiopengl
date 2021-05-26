package com.hiopengl.android.graphics.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hiopengl.android.graphics.drawer.OpenGLDrawer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private boolean mRunning = false;
    private SurfaceHolder mSurfaceHolder;
    private OpenGLDrawer mDrawer;

    public OpenGLSurfaceView(Context context) {
        super(context);
        initView();
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        //创建一个EGL实例
        EGL10 egl = (EGL10) EGLContext.getEGL();
        //
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //初始化EGLDisplay
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
        //选择config创建opengl运行环境
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];

        EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, null);
        //创建新的surface
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        mDrawer = new OpenGLDrawer();
        mRunning = true;
        while (mRunning) {
            synchronized (mSurfaceHolder) {
                mDrawer.draw(gl);

                //显示绘制结果到屏幕上
                egl.eglSwapBuffers(dpy, surface);
            }
            SystemClock.sleep(333);
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
    }
}
