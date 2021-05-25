package com.hiopengl.android.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Build;
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
import androidx.annotation.RequiresApi;

import com.hiopengl.R;
import com.hiopengl.advanced.model.Torus;
import com.hiopengl.base.ActionBarActivity;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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

    private IntBuffer mPboIds;
    private final int mPixelStride = 4;//RGBA 4字节
    private int mRowStride;//对齐4字节
    private int mPboIndex;
    private int mPboNewIndex;
    private int mPboSize;

    private boolean mInitRecord;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
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

        initPixelBuffer(mWidth, mHeight);

        mRunning = true;
        while (mRunning) {
            synchronized (mSurfaceHolder) {
                render(gl);

                if (mIsCapturing) {
                    bindPixelBuffer();
                }

                // 显示绘制结果到屏幕上
                egl.eglSwapBuffers(mEGLDisplay, mDrawSurface);
            }
            SystemClock.sleep(333);
        }

        destroyPixelBuffers();

        egl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(mEGLDisplay, mDrawSurface);
        egl.eglDestroyContext(mEGLDisplay, mEGLContext);
        egl.eglTerminate(mEGLDisplay);
    }

    public void onTakeScreenshotClick(View view) {
        synchronized (this) {
            mIsCapturing = true;
            mInitRecord = true;
        }
    }

    private void render(GL10 gl) {
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

        mTorus.draw(gl, mMVPMatrix);
    }

    //初始化2个pbo，交替使用
    public void initPixelBuffer(int width, int height) {
        if (mPboIds != null) {
            return;
        }

        //OpenGLES默认应该是4字节对齐应，但是不知道为什么在索尼Z2上效率反而降低
        //并且跟ImageReader最终计算出来的rowStride也和我这样计算出来的不一样，这里怀疑跟硬件和分辨率有关
        //这里默认取得128的倍数，这样效率反而高，为什么？
        final int align = 128;//128字节对齐
        mRowStride = (width * mPixelStride + (align - 1)) & ~(align - 1);

        mPboSize = mRowStride * height;

        mPboIds = IntBuffer.allocate(2);
        GLES30.glGenBuffers(2, mPboIds);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(0));
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(1));
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
    }

    private void destroyPixelBuffers() {
        if (mPboIds != null) {
            GLES30.glDeleteBuffers(2, mPboIds);
            mPboIds = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bindPixelBuffer() {
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(mPboIndex));
        GLES30.glReadPixels(0, 0, mRowStride / mPixelStride, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0);

        if (mInitRecord) { //第一帧没有数据跳出
            unbindPixelBuffer();
            mInitRecord = false;
            return;
        }

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(mPboNewIndex));

        // glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER, 0, mPboSize, GLES30.GL_MAP_READ_BIT);
        new ScreenshotThread(mHandler, byteBuffer).start();
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
        unbindPixelBuffer();

        mIsCapturing = false;
    }

    //解绑pbo
    private void unbindPixelBuffer() {
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);

        mPboIndex = (mPboIndex + 1) % 2;
        mPboNewIndex = (mPboNewIndex + 1) % 2;
    }

    private Bitmap getBitmapFromBuffer(ByteBuffer byteBuffer, int width, int height) {
        byte[] data = new byte[mPboSize];//创建byte
        byteBuffer.get(data);//将buffer数据写入byte中

        int pixelStride = 4;//像素个数，RGBA为4
        int rowStride = mRowStride;//这里除pixelStride就是真实宽度
        int rowPadding = rowStride - pixelStride * width;//计算多余宽度

        int[] pixelData = new int[width * height];

        int offset = 0;
        int index = 0;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int pixel = 0;
                pixel |= (data[offset] & 0xff) << 16;     // R
                pixel |= (data[offset + 1] & 0xff) << 8;  // G
                pixel |= (data[offset + 2] & 0xff);       // B
                pixel |= (data[offset + 3] & 0xff) << 24; // A
                pixelData[index++] = pixel;
                offset += pixelStride;
            }
            offset += rowPadding;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        bitmap = Bitmap.createBitmap(pixelData,
                width, height,
                Bitmap.Config.ARGB_8888);//创建bitmap

        return bitmap;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SHOW_SCREENSHOT:
                    mFrameView.setVisibility(View.VISIBLE);
                    mScreenshotView.setImageBitmap((Bitmap) msg.obj);
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

    private class ScreenshotThread extends Thread {

        private Handler mHandler;
        private ByteBuffer mBuffer;

        public ScreenshotThread(Handler handler, ByteBuffer byteBuffer) {
            mHandler = handler;
            mBuffer = byteBuffer;
        }

        @Override
        public void run() {
            Bitmap bmp = getBitmapFromBuffer(mBuffer, mRowStride / mPixelStride, mHeight);
            Message message = Message.obtain();
            message.what = MSG_SHOW_SCREENSHOT;
            message.obj = bmp;
            mHandler.sendMessage(message);
        }
    }
}