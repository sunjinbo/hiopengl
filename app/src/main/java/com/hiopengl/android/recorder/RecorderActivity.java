package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.CodecUtil;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;
import com.hiopengl.utils.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.widget.Toast;

import javax.microedition.khronos.opengles.GL10;

public abstract class RecorderActivity extends ActionBarActivity
        implements SurfaceHolder.Callback, Runnable, Choreographer.FrameCallback {

    protected static final float BALL_RADIUS = 100f;
    protected static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    /**
     * Constructor flag: surface must be recordable.  This discourages EGL from using a
     * pixel format that cannot be converted efficiently to something usable by the video
     * encoder.
     */
    public static final int FLAG_RECORDABLE = 0x01;

    /**
     * Constructor flag: ask for GLES3, fall back to GLES2 if not available.  Without this
     * flag, GLES2 is used.
     */
    public static final int FLAG_TRY_GLES3 = 0x02;

    // Android-specific extension.
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mSurfaceHolder;
    protected Playground mPlayground;
    protected int mWidth;
    protected int mHeight;

    protected int mProgram = -1;
    protected int mTextureId = -1;
    protected FloatBuffer mVertexBuffer;
    protected float mVertexArray[] = {
            -0.5f, -0.4f, 0.0f, BALL_RADIUS * 2 };

    protected boolean mIsRunning = false;

    protected Button mRecordButton;
    protected boolean mIsRecording = false;

    protected long mLastTickTimeMillis = 0;

    protected VideoEncoderCore mEncoderCore;
    protected TextureMovieEncoder2 mVideoEncoder;

    protected EGLSurface mEncoderSurface;
    protected EGLSurface mScreenSurface;

    protected RenderHandler mRenderHandler;

    protected EGLDisplay mEGLDisplay;
    protected EGLContext mEGLContext;
    protected EGLConfig mEGLConfig;

    private Object mReadyFence = new Object();      // guards ready/running
    private boolean mReady;

    private File mOutputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        mRecordButton = findViewById(R.id.recorder);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    mIsRecording = false;
                    mRecordButton.setText("START RECORD");
                    mRenderHandler.stopRecord();
                } else {
                    mIsRecording = true;
                    mRecordButton.setText("STOP RECORD");
                    mRenderHandler.startRecord();
                }
            }
        });

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);

        ShaderUtil.setEGLContextClientVersion(3);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertexArray.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(mVertexArray);
        mVertexBuffer.position(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRenderHandler != null) {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Choreographer.getInstance().removeFrameCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
    }

    @Override
    public void run() {
        synchronized (mReadyFence) {
            mReady = true;
            mReadyFence.notify();
        }

        Looper.prepare();

        initEGL();

        int[] surfaceAttribs = { EGL14.EGL_NONE };
        mScreenSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurfaceHolder,
                surfaceAttribs, 0);
        EGL14.eglMakeCurrent(mEGLDisplay, mScreenSurface, mScreenSurface, mEGLContext);

        onSizeChanged(mWidth, mHeight);

        initProgram();

        mRenderHandler = new RenderHandler();
        Choreographer.getInstance().postFrameCallback(this);
        Looper.loop();

        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEGLDisplay, mScreenSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        EGL14.eglTerminate(mEGLDisplay);
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
        while (!mReady) {
            try {
                mReadyFence.wait();
            } catch (InterruptedException ie) {
                // ignore
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        // start the draw events
        Choreographer.getInstance().postFrameCallback(this);
        mRenderHandler.doFrame(frameTimeNanos);
    }

    abstract void drawFrame(long frameTimeNanos);

    protected void onSizeChanged(int width, int height) {

    }

    protected void initEGL() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        EGLConfig config = getConfig(FLAG_RECORDABLE | FLAG_TRY_GLES3, 3);
        int[] attrib3_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, EGL14.EGL_NO_CONTEXT,
                attrib3_list, 0);
        if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
            //Log.d(TAG, "Got GLES 3 config");
            mEGLConfig = config;
            mEGLContext = context;
        }
    }

    protected void initProgram() {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        mPlayground = new Playground(mWidth, mHeight);

        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(this, "vertex_recorder.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(this, "fragment_recorder.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        // 生成ball纹理
        mTextureId = GlUtil.loadTexture(this, R.drawable.ball);
    }

    protected EGLConfig getConfig(int flags, int version) {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
        if (version >= 3) {
            renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        }

        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL14.EGL_NONE
        };
        if ((flags & FLAG_RECORDABLE) != 0) {
            attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
            attribList[attribList.length - 2] = 1;
        }
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            return null;
        }
        return configs[0];
    }

    protected void drawPlayground() {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);

        // 设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        // 将纹理单元传递片段着色器的u_TextureUnit
        int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
        GLES30.glUniform1i(uTextureLocation, 0);

        GLES30.glVertexAttribPointer(0, 4, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
    }

    protected void tick() {
        mLastTickTimeMillis = SystemClock.elapsedRealtime();

        mPlayground.ball.step();
        mVertexArray[0] = mPlayground.ball.getX();
        mVertexArray[1] = mPlayground.ball.getY();

        mVertexBuffer.put(mVertexArray);
        mVertexBuffer.position(0);
    }

    protected void startRecording() {
        mOutputFile = new File(getExternalCacheDir(), TimeUtil.getCurrentTime() + ".mp4");
        try {
            int width = CodecUtil.getSize(mWidth);
            int height = CodecUtil.getSize(mHeight);
            mEncoderCore = new VideoEncoderCore(width, height, 4000000, mOutputFile);
            mVideoEncoder = new TextureMovieEncoder2(mEncoderCore);
            int[] surfaceAttribs = { EGL14.EGL_NONE };
            mEncoderSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mEncoderCore.getInputSurface(),
                    surfaceAttribs, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    protected void stopRecording() {
        if (mVideoEncoder != null) {
            if (mVideoEncoder.isRecording()) {
                mVideoEncoder.stopRecording();
            }
            mVideoEncoder = null;
        }

        if (mEncoderSurface != null) {
            EGL14.eglDestroySurface(mEGLDisplay, mEncoderSurface);
            mEncoderSurface = null;
        }
    }

    public class RenderHandler extends Handler {

        private final static int MSG_START_RECORD = 1;
        private final static int MSG_STOP_RECORD = 2;
        private final static int MSG_DO_FRAME = 3;

        public void startRecord() {
            sendEmptyMessage(MSG_START_RECORD);
        }

        public void stopRecord() {
            sendEmptyMessage(MSG_STOP_RECORD);
        }

        public void doFrame(long frameTimeNanos) {
            sendMessage(obtainMessage(RenderHandler.MSG_DO_FRAME,
                    (int) (frameTimeNanos >> 32), (int) frameTimeNanos));
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START_RECORD:
                    startRecording();
                    break;
                case MSG_STOP_RECORD:
                    stopRecording();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RecorderActivity.this, mOutputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case MSG_DO_FRAME:
                    long timestamp = (((long) msg.arg1) << 32) |
                            (((long) msg.arg2) & 0xffffffffL);
                    drawFrame(timestamp);
                    break;
            }
        }
    }

    public class Ball {
        public Playground playground;
        public float x;
        public float y;
        public float dx;
        public float dy;
        public float radius;

        public Ball(Playground playground, float x, float y, float dx, float dy, float radius) {
            this.playground = playground;
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.radius = radius;
        }

        public void step() {
            this.x += dx;
            this.y += dy;

            // 开始边缘碰撞检测
            if (this.x - radius <= 0) {
                this.x = radius;
                this.dx *= -1;
            } else if (this.x + radius >= playground.width) {
                this.x = playground.width - radius;
                this.dx *= -1;
            }

            if (this.y - radius <= 0) {
                this.y = radius;
                this.dy *= -1;
            } else if (this.y + radius >= playground.height) {
                this.y = playground.height - radius;
                this.dy *= -1;
            }
        }

        public float getX() {
            return (x / playground.width) * 2f - 1f;
        }

        public float getY() {
            return (y / playground.height) * 2f - 1f;
        }
    }

    public class Playground {
        public float width;
        public float height;
        public Ball ball;

        private Random random = new Random();

        public Playground(float width, float height) {
            this.width = width;
            this.height = height;
            this.ball = generate(width, height, BALL_RADIUS);
        }

        private Ball generate(float width, float height, float radius) {
            Ball b = new Ball(this,
                    (random.nextInt((int) (width - radius * 2)) + radius),
                    (random.nextInt((int) (height - radius * 2)) + radius),
                    9f,
                    -11f,
                    radius);
            return b;
        }
    }
}