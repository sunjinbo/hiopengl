package com.hiopengl.android.recorder;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.os.Bundle;

import com.hiopengl.utils.GlUtil;

public class FBOBlitx2Activity extends RecorderActivity {

    // Used for off-screen rendering.
    private int mOffscreenTexture;
    private int mFramebuffer;
    private int mDepthBuffer;
    private TextureDrawer mTextureDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    void drawFrame(long frameTimeNanos) {
        tick();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        drawPlayground();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        drawTexture(mOffscreenTexture);
        EGL14.eglSwapBuffers(mEGLDisplay, mScreenSurface);

        if (mVideoEncoder != null && mVideoEncoder.isRecording()) {
            mVideoEncoder.frameAvailableSoon();
            EGL14.eglMakeCurrent(mEGLDisplay, mEncoderSurface, mEncoderSurface, mEGLContext);

            drawTexture(mOffscreenTexture);
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEncoderSurface, frameTimeNanos);
            EGL14.eglSwapBuffers(mEGLDisplay, mEncoderSurface);

            EGL14.eglMakeCurrent(mEGLDisplay, mScreenSurface, mScreenSurface, mEGLContext);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height) {
        prepareFramebuffer(width, height);
        mTextureDrawer = new TextureDrawer();
    }

    private void drawTexture(int textureId) {
        mTextureDrawer.drawTexture(textureId);
    }

    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("prepareFramebuffer start");

        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture " + mOffscreenTexture);

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        // Create a depth buffer and bind it.
        GLES20.glGenRenderbuffers(1, values, 0);
        GlUtil.checkGlError("glGenRenderbuffers");
        mDepthBuffer = values[0];    // expected > 0
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer);
        GlUtil.checkGlError("glBindRenderbuffer " + mDepthBuffer);

        // Allocate storage for the depth buffer.
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                width, height);
        GlUtil.checkGlError("glRenderbufferStorage");

        // Attach the depth buffer and the texture (color buffer) to the framebuffer object.
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, mDepthBuffer);
        GlUtil.checkGlError("glFramebufferRenderbuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GlUtil.checkGlError("prepareFramebuffer done");
    }
}
