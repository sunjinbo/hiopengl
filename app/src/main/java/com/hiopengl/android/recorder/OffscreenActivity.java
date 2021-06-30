package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.CodecUtil;
import com.hiopengl.utils.DisplayUtil;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;
import com.hiopengl.utils.TimeUtil;
import com.hiopengl.utils.Timer;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class OffscreenActivity extends ActionBarActivity
        implements Runnable, Choreographer.FrameCallback, Timer.Callback {

    private static final int FLAG_RECORDABLE = 0x01;
    private static final int FLAG_TRY_GLES3 = 0x02;
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private enum State {
        NotStart,
        Recording,
        Playing,
        Error
    }

    private State mCurrentState = State.NotStart;

    private VideoView mVideoView;
    private ProgressBar mProgressBar;

    private VideoEncoderCore mEncoderCore;
    private TextureMovieEncoder2 mVideoEncoder;

    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLConfig mEGLConfig;

    private EGLSurface mEncoderSurface;
    private EGLSurface mOffScreenSurface;

    private RenderHandler mRenderHandler;

    private Object mReadyFence = new Object();      // guards ready/running
    private boolean mReady;

    private boolean mIsRunning = false;
    private boolean mIsDestroy = false;

    private File mOutputFile;

    private int mWidth;
    private int mHeight;

    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        ShaderUtil.setEGLContextClientVersion(3);
        mVideoView = findViewById(R.id.video_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mTimer = new Timer(5, this);
        mRenderHandler = new RenderHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentState == State.NotStart) {
            mCurrentState = State.Recording;
            new Thread(this).start();
            mWidth = DisplayUtil.dp2px(this, 240);
            mHeight = DisplayUtil.dp2px(this, 240);

            mRenderHandler.startRecord();
            mTimer.start();
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
        mIsDestroy = true;
    }

    @Override
    public void onTimerExpired() {
        mRenderHandler.stopRecord();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mVideoView.setVideoPath(mOutputFile.getAbsolutePath());
                mVideoView.start();
            }
        });
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
        mOffScreenSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
                surfaceAttribs, 0);
        EGL14.eglMakeCurrent(mEGLDisplay, mOffScreenSurface, mOffScreenSurface, mEGLContext);

        initProgram();

        Choreographer.getInstance().postFrameCallback(this);
        Looper.loop();

        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEGLDisplay, mOffScreenSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        EGL14.eglTerminate(mEGLDisplay);
    }

    private void startRecording() {
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

    private void stopRecording() {
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

    @Override
    public void doFrame(long frameTimeNanos) {
        Log.d("record", "doFrame(long frameTimeNanos)");
        if (!mIsDestroy) {
            // start the draw events
            Choreographer.getInstance().postFrameCallback(this);
            mRenderHandler.doFrame(frameTimeNanos);
        }
    }

    private void drawFrame(long frameTimeNanos) {
        if (mVideoEncoder != null && mVideoEncoder.isRecording()) {
            mVideoEncoder.frameAvailableSoon();

            EGL14.eglMakeCurrent(mEGLDisplay, mEncoderSurface, mEncoderSurface, mEGLContext);

            GLES30.glViewport(0, 0, mWidth, mHeight);
            GLES30.glClearColor(0.0F, 1.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEncoderSurface, frameTimeNanos);

            EGL14.eglSwapBuffers(mEGLDisplay, mEncoderSurface);

//            EGL14.eglMakeCurrent(mEGLDisplay, mOffScreenSurface, mOffScreenSurface, mEGLContext);
        }
    }

    private void initEGL() {
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

    private void initProgram() {
        GLES30.glViewport(0, 0, mWidth, mHeight);

    }

    private EGLConfig getConfig(int flags, int version) {
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

    public class RenderHandler extends Handler {

        private final static int MSG_START_RECORD = 1;
        private final static int MSG_STOP_RECORD = 2;
        private final static int MSG_DO_FRAME = 3;

        public void prepare() {
            sendEmptyMessage(MSG_START_RECORD);
        }

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
                    synchronized (mReadyFence) {
                        while (!mReady) {
                            try {
                                mReadyFence.wait();
                            } catch (InterruptedException ie) {
                                // ignore
                            }
                        }
                    }
                    startRecording();
                    break;
                case MSG_STOP_RECORD:
                    stopRecording();
                    runOnUiThread(() -> Toast.makeText(OffscreenActivity.this, mOutputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                    break;
                case MSG_DO_FRAME:
                    long timestamp = (((long) msg.arg1) << 32) |
                            (((long) msg.arg2) & 0xffffffffL);
                    drawFrame(timestamp);
                    break;
            }
        }
    }
}