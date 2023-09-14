package com.hiopengl.advanced.tasks;

import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_DEPTH_SIZE;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;

import android.content.Context;
import android.opengl.EGLExt;
import android.view.SurfaceHolder;

import com.hiopengl.android.graphics.drawer.TextureDrawer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class RenderGL implements Runnable {

    private final Context mContext;
    private boolean mIsRunning = false;
    private final SurfaceHolder mSurfaceHolder;
    private EGLContext mSharedContext;
    private TextureDrawer mDrawer;
    private int mWidth, mHeight;
    private int mSharedTextureId;

    // Used to wait for the thread to start.
    private final Object mStartLock = new Object();
    private boolean mReady = false;

    public RenderGL(Context context, SurfaceHolder surfaceHolder) {
        mContext = context;
        mSurfaceHolder = surfaceHolder;
    }

    public void startGL() {
        new Thread(this).start();
    }

    public void setRenderSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setSharedContext(EGLContext sharedContext, int sharedTextureId) {
        mSharedContext = sharedContext;
        mSharedTextureId = sharedTextureId;

        synchronized (mStartLock) {
            mReady = true;
            mStartLock.notify();
        }
    }

    @Override
    public void run() {
        synchronized (mStartLock) {
            while (!mReady) {
                try {
                    mStartLock.wait();
                } catch (InterruptedException ie) { /* not expected */ }
            }
        }

        //创建一个EGL实例
        EGL10 egl = (EGL10) EGLContext.getEGL();
        //
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //初始化EGLDisplay
        int[] version = new int[2];
        egl.eglInitialize(dpy, version);

        int[] configSpec = {
                EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL_RED_SIZE, 5,
                EGL_GREEN_SIZE, 6,
                EGL_BLUE_SIZE, 5,
                EGL_DEPTH_SIZE, 1,
                EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        //选择config创建opengl运行环境
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];
        int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(dpy, config,
                mSharedContext, attrib_list);
        //创建新的surface
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        mDrawer = new TextureDrawer(mContext, mSharedTextureId);
        mDrawer.setSize(gl, mWidth, mHeight);

        mIsRunning = true;
        while (mIsRunning) {
            synchronized (mSurfaceHolder) {
                mDrawer.draw(gl);

                //显示绘制结果到屏幕上
                egl.eglSwapBuffers(dpy, surface);
            }
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);

        synchronized (mStartLock) {
            mReady = false;
        }
    }
}
