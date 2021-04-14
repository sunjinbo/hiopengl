package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.os.Bundle;

public class DrawTwiceActivity extends RecorderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    void drawFrame(long frameTimeNanos) {
        tick();

        drawPlayground();

        //显示绘制结果到屏幕上
        EGL14.eglSwapBuffers(mEGLDisplay, mScreenSurface);

        if (mVideoEncoder != null) {
            if (mVideoEncoder.isRecording()) {
                mVideoEncoder.frameAvailableSoon();

                EGL14.eglMakeCurrent(mEGLDisplay, mEncoderSurface, mEncoderSurface, mEGLContext);

                drawPlayground();

                EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEncoderSurface, frameTimeNanos);

                EGL14.eglSwapBuffers(mEGLDisplay, mEncoderSurface);

                EGL14.eglMakeCurrent(mEGLDisplay, mScreenSurface, mScreenSurface, mEGLContext);
            }
        }
    }
}
