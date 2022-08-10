package com.hiopengl.android.graphics.view;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.LogUtil;

public class OpenGLProducer implements Runnable {

    private EGLContext mSharedContext;
    private SurfaceTexture mSurfaceTexture;
    private int mWidth, mHeight;
    private boolean mIsRunning = false;

    public OpenGLProducer(EGLContext context, SurfaceTexture surfaceTexture, int width, int height) {
        mSharedContext = context;
        mSurfaceTexture = surfaceTexture;
        mWidth = width;
        mHeight = height;
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        mIsRunning = false;
    }

    @Override
    public void run() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
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
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];

        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,// 0x3098
                EGL14.EGL_NONE
        };

        EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, ctxAttr);
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceTexture, null);

        egl.eglMakeCurrent(dpy, surface, surface, context);
        GL10 gl = (GL10)context.getGL();
        int err = egl.eglGetError();
        LogUtil.d("err = " + err);
        mIsRunning = true;
        while (mIsRunning) {
            try {
                synchronized (this) {
                    GLES30.glViewport(0, 0, mWidth, mHeight);
                    GLES30.glClearColor(1.0F, 0.0F, 0.0F, 1.0F); // draw red background
                    GlUtil.checkGl3Error("clearColor");
                    GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                            | GL10.GL_DEPTH_BUFFER_BIT);

                    // swap buffers to SurfaceTexture
                    egl.eglSwapBuffers(dpy, surface);
                    err = egl.eglGetError();
                    LogUtil.d("err = " + err);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            SystemClock.sleep(333);

            GLES30.glFinish();
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
    }
}
