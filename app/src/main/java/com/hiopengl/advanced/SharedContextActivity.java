package com.hiopengl.advanced;

import android.opengl.EGLExt;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hiopengl.R;
import com.hiopengl.android.graphics.drawer.CubeDrawer;
import com.hiopengl.android.graphics.drawer.OpenGLDrawer;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.base.NotImplementationActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_context);
        mSurfaceView1 = findViewById(R.id.surface_view_1);
        mSurfaceView1.getHolder().addCallback(new SurfaceView1Renderer());
        mSurfaceView2 = findViewById(R.id.surface_view_2);
    }

    private class SurfaceView1Renderer implements SurfaceHolder.Callback, Runnable {

        private boolean mIsRunning = false;
        private SurfaceHolder mSurfaceHolder;
        private CubeDrawer mDrawer;
        private int mWidth, mHeight;

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

//            int texId = loadTexture();
//            SurfaceView2Renderer renderer = new SurfaceView2Renderer(context, texId);
//            mSurfaceView2.getHolder().addCallback(renderer);

            mDrawer = new CubeDrawer(SharedContextActivity.this);
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
        }

        private int loadTexture() {
            return 0;
        }
    }

    private class SurfaceView2Renderer implements SurfaceHolder.Callback, Runnable {

        private boolean mIsRunning = false;
        private SurfaceHolder mSurfaceHolder;
        private EGLContext mSharedContext;
        private int mSharedTextureId;

        public SurfaceView2Renderer(EGLContext sharedContext, int sharedTextureId) {
            mSharedContext = sharedContext;
            mSharedTextureId = sharedTextureId;
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
                    mSharedContext, null);
            //创建新的surface
            EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
            //将opengles环境设置为当前
            egl.eglMakeCurrent(dpy, surface, surface, context);
            //获取当前opengles画布
            GL10 gl = (GL10)context.getGL();

            mIsRunning = true;
            while (mIsRunning) {
                synchronized (mSurfaceHolder) {

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
}