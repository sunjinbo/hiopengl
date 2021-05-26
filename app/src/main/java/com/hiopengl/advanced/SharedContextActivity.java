package com.hiopengl.advanced;

import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hiopengl.R;
import com.hiopengl.android.graphics.drawer.CubeDrawer;
import com.hiopengl.android.graphics.drawer.TextureDrawer;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_DEPTH_SIZE;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;

public class SharedContextActivity extends ActionBarActivity {

    private SurfaceView mSurfaceView1;
    private SurfaceView mSurfaceView2;

    private SurfaceView1Renderer mSurfaceView1Renderer;
    private SurfaceView2Renderer mSurfaceView2Renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_context);
        mSurfaceView1 = findViewById(R.id.surface_view_1);
        mSurfaceView1Renderer = new SurfaceView1Renderer();
        mSurfaceView1.getHolder().addCallback(mSurfaceView1Renderer);

        mSurfaceView2 = findViewById(R.id.surface_view_2);
        mSurfaceView2Renderer = new SurfaceView2Renderer();
        mSurfaceView2.getHolder().addCallback(mSurfaceView2Renderer);
    }

    private class SurfaceView1Renderer implements SurfaceHolder.Callback, Runnable {

        private boolean mIsRunning = false;
        private SurfaceHolder mSurfaceHolder;
        private CubeDrawer mDrawer;
        private int mWidth, mHeight;

        private int mFramebuffer;
        private int mOffscreenTexture;

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            new Thread(this).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceHolder = holder;
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsRunning = false;
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
                    EGL10.EGL_NO_CONTEXT, attrib_list);
            //创建新的surface
            EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
            //将opengles环境设置为当前
            egl.eglMakeCurrent(dpy, surface, surface, context);
            //获取当前opengles画布
            GL10 gl = (GL10)context.getGL();

            mDrawer = new CubeDrawer(SharedContextActivity.this);
            GlUtil.checkGl3Error("check CubeDrawer");
            mDrawer.setSize(gl, mWidth, mHeight);

            initFrameBuffer();

            mSurfaceView2Renderer.setSharedContext(context, mOffscreenTexture);

            mIsRunning = true;
            while (mIsRunning) {
                synchronized (mSurfaceHolder) {
                    GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, mFramebuffer);

                    mDrawer.draw(gl);

                    GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);

                    GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, mFramebuffer);

                    GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
                    GlUtil.checkGl3Error("glReadBuffer");

                    GLES30.glBlitFramebuffer(0, 0, mWidth, mHeight,
                            0, 0, mWidth, mHeight,
                            GLES30.GL_COLOR_BUFFER_BIT,
                            GLES30.GL_NEAREST);

                    //显示绘制结果到屏幕上
                    egl.eglSwapBuffers(dpy, surface);
                }
            }

            egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(dpy, surface);
            egl.eglDestroyContext(dpy, context);
            egl.eglTerminate(dpy);
        }

        private void initFrameBuffer() {
            GlUtil.checkGl3Error("initFrameBuffer");

            final int[] ids = new int[1];
            GLES30.glGenFramebuffers(1, ids, 0);
            GlUtil.checkGl3Error("glGenFramebuffers");
            mFramebuffer = ids[0];
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);
            GlUtil.checkGl3Error("glBindFramebuffer " + mFramebuffer);

            GLES30.glGenTextures(1, ids, 0);
            GlUtil.checkGlError("glGenTextures");
            mOffscreenTexture = ids[0];   // expected > 0
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mOffscreenTexture);
            GlUtil.checkGlError("glBindTexture " + mOffscreenTexture);

            // Create texture storage.
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0,
                    GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

            // Set parameters.  We're probably using non-power-of-two dimensions, so
            // some values may not be available for use.
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_NEAREST);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
                    GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
                    GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
                    GLES30.GL_CLAMP_TO_EDGE);
            GlUtil.checkGlError("glTexParameter");
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D, mOffscreenTexture, 0);
            GlUtil.checkGlError("glFramebufferTexture2D");

            int status = GLES30.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("framebuffer is not complete!");
            }

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }
    }

    private class SurfaceView2Renderer implements SurfaceHolder.Callback, Runnable {

        private boolean mIsRunning = false;
        private SurfaceHolder mSurfaceHolder;
        private EGLContext mSharedContext;
        private TextureDrawer mDrawer;
        private int mWidth, mHeight;
        private int mSharedTextureId;


        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

        public void setSharedContext(EGLContext sharedContext, int sharedTextureId) {
            mSharedContext = sharedContext;
            mSharedTextureId = sharedTextureId;

            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            new Thread(this).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceHolder = holder;
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsRunning = false;
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

            mDrawer = new TextureDrawer(SharedContextActivity.this, mSharedTextureId);
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
}