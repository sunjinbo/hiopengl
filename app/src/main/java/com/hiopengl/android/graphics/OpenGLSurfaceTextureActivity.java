package com.hiopengl.android.graphics;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.SystemClock;
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

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

public class OpenGLSurfaceTextureActivity extends ActionBarActivity
        implements SurfaceHolder.Callback, Runnable, SurfaceTexture.OnFrameAvailableListener {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceTexture mSurfaceTexture;
    private boolean mRunning = false;
    private int mWidth, mHeight;

    private TextureDrawer mDrawer;
    private OpenGLProducer mOpenGLProducer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_surface_texture);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
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
        mRunning = false;
        if (mOpenGLProducer != null) {
            mOpenGLProducer.stop();
        }
    }

    @Override
    public void run() {
        //创建一个EGL实例
        EGL10 egl = (EGL10) EGLContext.getEGL();
        //
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
        // 选择config创建OpenGL运行环境
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];

        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,// 0x3098
                EGL14.EGL_NONE
        };

        EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, ctxAttr);

        //创建新的surface
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        final int textureId = generateTexture(mWidth, mHeight);
        mSurfaceTexture = new SurfaceTexture(textureId);
        mOpenGLProducer = new OpenGLProducer(this, mSurfaceTexture, mWidth, mHeight);
        mOpenGLProducer.start();

        mDrawer = new TextureDrawer(this, textureId);
        mRunning = true;
        while (mRunning) {
            synchronized (mSurfaceHolder) {
                GLES30.glViewport(0, 0, mWidth, mHeight);
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

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceTexture.updateTexImage();
    }

    private int generateTexture(int width, int height) {
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES30.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, values[0]);
        GlUtil.checkGlError("glBindTexture " + values[0]);

        // Create texture storage.
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
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

        return values[0];
    }

    private final class OpenGLProducer implements Runnable {

        private Context mContext;
        private SurfaceTexture mSurfaceTexture;
        private CubeDrawer mDrawer;
        private int mWidth, mHeight;
        private boolean mIsRunning = false;

        public OpenGLProducer(Context context, SurfaceTexture surfaceTexture, int width, int height) {
            mContext = context;
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
            //创建一个EGL实例
            EGL10 egl = (EGL10) EGLContext.getEGL();
            //
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
            // 选择config创建OpenGL运行环境
            egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
            EGLConfig config = configs[0];

            int ctxAttr[] = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,// 0x3098
                    EGL14.EGL_NONE
            };

            EGLContext context = egl.eglCreateContext(dpy, config,
                    EGL10.EGL_NO_CONTEXT, ctxAttr);

            //创建新的surface
            EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceTexture, null);
            //将opengles环境设置为当前
            egl.eglMakeCurrent(dpy, surface, surface, context);
            //获取当前opengles画布
            GL10 gl = (GL10)context.getGL();

            mDrawer = new CubeDrawer(mContext);
            mDrawer.setSize(gl, mWidth, mHeight);
            mIsRunning = true;
            while (mIsRunning) {
                synchronized (mSurfaceTexture) {
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
}