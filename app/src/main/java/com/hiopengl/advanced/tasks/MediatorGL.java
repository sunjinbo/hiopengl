package com.hiopengl.advanced.tasks;

import android.graphics.SurfaceTexture;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class MediatorGL implements Runnable {

    public interface Callback {
        void onInitGL(EGLContext eglContext);
    }

    private final Callback mCallback;
    private final Object mReadyFence = new Object();

    private SurfaceTexture mSurfaceTexture;

    private boolean mRunning = false;

    public MediatorGL(Callback callback) {
        mCallback = callback;
    }

    public void startGL() {
        new Thread(this).start();
    }

    public void setupSurfaceTexture(SurfaceTexture surfaceTexture) {
        synchronized (mReadyFence) {
            mSurfaceTexture = surfaceTexture;
            mReadyFence.notify();
        }
    }

    @Override
    public void run() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
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


        mCallback.onInitGL(context);

        waitUntilReady();

        //创建新的surface
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceTexture, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        mRunning = true;
        while (mRunning) {
            SystemClock.sleep(333);
            synchronized (mReadyFence) {
                onRender(gl);

                //显示绘制结果到屏幕上
                egl.eglSwapBuffers(dpy, surface);
            }
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
    }

    private void onRender(GL10 gl) {
        gl.glClearColor(1.0F, 0.0F, 0.0F, 1.0F);
        // Clears the screen and depth buffer.
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);
    }

    private void waitUntilReady() {
        synchronized (mReadyFence) {
            try {
                mReadyFence.wait();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
