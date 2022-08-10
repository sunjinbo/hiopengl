package com.hiopengl.android.graphics;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;

import com.hiopengl.R;
import com.hiopengl.advanced.SharedContextActivity;
import com.hiopengl.android.graphics.drawer.CubeDrawer;
import com.hiopengl.android.graphics.drawer.ExternalOESTextureDrawer;
import com.hiopengl.android.graphics.drawer.OpenGL3Drawer;
import com.hiopengl.android.graphics.drawer.OpenGLDrawer;
import com.hiopengl.android.graphics.drawer.TextureDrawer;
import com.hiopengl.android.graphics.view.OpenGLProducer;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.LogUtil;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGLExt.EGL_RECORDABLE_ANDROID;

public class OpenGLSurfaceTextureActivity extends ActionBarActivity
        implements Runnable, TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private SurfaceTexture mOutputSurfaceTexture;
    private boolean mRunning = false;
    private int mWidth, mHeight;

    private OpenGL3Drawer mDrawer;
    private OpenGLProducer mOpenGLProducer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_surface_texture);
        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int w, int h) {
        mSurfaceTexture = surfaceTexture;
        mWidth = w;
        mHeight = h;
        new Thread(this).start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int w, int h) {
        mSurfaceTexture = surfaceTexture;
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mRunning = false;
        if (mOpenGLProducer != null) {
            mOpenGLProducer.stop();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

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

        final int textureId = generateOESTexture(mWidth, mHeight);

        mOutputSurfaceTexture = new SurfaceTexture(textureId, false);
        mOutputSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                LogUtil.d("onFrameAvailable()");
            }
        });
        mOpenGLProducer = new OpenGLProducer(context, mOutputSurfaceTexture, mWidth, mHeight);
        mOpenGLProducer.start();
        mDrawer = new ExternalOESTextureDrawer(this, textureId);

        mRunning = true;
        while (mRunning) {
            synchronized (this) {
                GLES30.glViewport(0, 0, mWidth, mHeight);

                gl.glClearColor(1.0F, 0.0F, 1.0F, 1.0F); // draw pink background
                // Clears the screen and depth buffer.
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                        | GL10.GL_DEPTH_BUFFER_BIT);

//                mDrawer.draw(gl); // draw red texture

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

    private int generateOESTexture(int width, int height) {
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES30.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, values[0]);
        GlUtil.checkGl3Error("glBindTexture " + values[0]);

        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GlUtil.checkGl3Error("glTexParameteri");

        // Create texture storage.
//        GLES30.glTexImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                0,
//                GLES11Ext.GL_RGBA8_OES,
//                width,
//                height,
//                0,
//                GLES11Ext.GL_RGBA8_OES,
//                GLES30.GL_UNSIGNED_BYTE,
//                null);
//        GlUtil.checkGl3Error("glTexImage2D");

        return values[0];
    }
}