package com.hiopengl.android.recorder;

import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.hiopengl.R;

public class OffscreenRecorderActivity extends RecorderActivity implements MediaPlayer.OnCompletionListener {

    private static final long VIDEO_DURATION = 12000L; // milliseconds
    private static final long CODEC_SPEED = 4;
    private static final long CODEC_DURATION = VIDEO_DURATION / CODEC_SPEED; // milliseconds

    private VideoView mVideoView;
    private ProgressBar mProgressBar;
    private OffscreenHandler mOffscreenHandler;
    private boolean mIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offscreen);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setOnCompletionListener(this);
        mProgressBar = findViewById(R.id.progress_bar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOffscreenHandler == null) {
            mOffscreenHandler = new OffscreenHandler();
            mOffscreenHandler.start();
        }
    }

    @Override
    public void tickFrame() {
        mIsRunning = true;
        new Thread(() -> {
            long startTime = SystemClock.elapsedRealtimeNanos();
            while (mIsRunning) {
                // start the draw events
                SystemClock.sleep(8);
                long internalTime = SystemClock.elapsedRealtimeNanos() - startTime;
                internalTime *= 4;
                mRenderHandler.doFrame(startTime + internalTime);
            }
        }).start();
    }

    @Override
    public void drawFrame(long frameTimeNanos) {
        Log.d(TAG, "doFrame(long frameTimeNanos) - " + frameTimeNanos);

        if (mIsRecording) {
            for (int i = 0; i < CODEC_SPEED; i++) {
                tick();
            }

            drawPlayground();

            //显示绘制结果到屏幕上
            EGL14.eglSwapBuffers(mEGLDisplay, mWindowsSurface);

            if (mVideoEncoder != null && mVideoEncoder.isRecording() && mEncoderSurface != null) {
                mVideoEncoder.frameAvailableSoon();

                EGL14.eglMakeCurrent(mEGLDisplay, mEncoderSurface, mEncoderSurface, mEGLContext);

                drawPlayground();

                EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEncoderSurface, frameTimeNanos);

                EGL14.eglSwapBuffers(mEGLDisplay, mEncoderSurface);

                EGL14.eglMakeCurrent(mEGLDisplay, mWindowsSurface, mWindowsSurface, mEGLContext);
            }
        }
    }

    @Override
    EGLSurface createSurface() {
        int[] surfaceAttribs = { EGL14.EGL_NONE };
        return EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttribs, 0);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playVideo();
    }

    private void playVideo() {
        mVideoView.setVideoPath(mOutputFile.getAbsolutePath());
        mVideoView.seekTo(0);
        mVideoView.requestFocus();
        mVideoView.start();
    }

    private class OffscreenHandler extends Handler {
        private final static int MSG_CREATE_RECORDER = 1;
        private final static int MSG_PREPARE_RECORDER = 2;
        private final static int MSG_START_RECORDER = 3;
        private final static int MSG_STOP_RECORDER = 4;
        private final static int MSG_RELEASE_RECORDER = 5;

        public void start() {
            sendEmptyMessage(MSG_CREATE_RECORDER);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_CREATE_RECORDER:
                    startRecorderLooper();
                    mWidth = mVideoView.getWidth();
                    mHeight = mVideoView.getHeight();
                    sendEmptyMessageDelayed(MSG_PREPARE_RECORDER, 333);
                    break;
                case MSG_PREPARE_RECORDER:
                    waitUntilLooperReady();
                    sendEmptyMessageDelayed(MSG_START_RECORDER, 333);
                    break;
                case MSG_START_RECORDER:
                    startRecording();
                    sendEmptyMessageDelayed(MSG_STOP_RECORDER, CODEC_DURATION);
                    break;
                case MSG_STOP_RECORDER:
                    stopRecording();
                    mIsRunning = false;
                    sendEmptyMessageDelayed(MSG_RELEASE_RECORDER, 555);
                    break;
                case MSG_RELEASE_RECORDER:
                    stopRecorderLooper();
                    mVideoView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    playVideo();
                    break;
                default:
                    break;
            }
        }
    }
}