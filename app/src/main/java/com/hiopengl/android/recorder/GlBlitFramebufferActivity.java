package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.os.Bundle;

public class GlBlitFramebufferActivity extends RecorderActivity {

    private int mScreenWidth = -1;
    private int mScreenHeight = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    void drawFrame(long frameTimeNanos) {
        tick();

        drawPlayground();

        if (mVideoEncoder != null && mVideoEncoder.isRecording()) {
            mVideoEncoder.frameAvailableSoon();

            EGL14.eglMakeCurrent(mEGLDisplay, mEncoderSurface, mScreenSurface, mEGLContext);

            GLES30.glBlitFramebuffer(
                    0, 0, getWidth(), getHeight(),
                    0, 0, getWidth(), getHeight(),
                    GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_NEAREST);

            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEncoderSurface, frameTimeNanos);

            EGL14.eglSwapBuffers(mEGLDisplay, mEncoderSurface);

            EGL14.eglMakeCurrent(mEGLDisplay, mScreenSurface, mScreenSurface, mEGLContext);
        }

        EGL14.eglSwapBuffers(mEGLDisplay, mScreenSurface);
    }

    public int getWidth() {
        if (mScreenWidth < 0) {
            return querySurface(mScreenSurface, EGL14.EGL_WIDTH);
        } else {
            return mScreenWidth;
        }
    }

    public int getHeight() {
        if (mScreenHeight < 0) {
            return querySurface(mScreenSurface, EGL14.EGL_HEIGHT);
        } else {
            return mScreenHeight;
        }
    }

    public int querySurface(EGLSurface eglSurface, int what) {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0);
        return value[0];
    }
}
