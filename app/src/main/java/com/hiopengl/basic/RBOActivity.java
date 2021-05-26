package com.hiopengl.basic;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.advanced.model.Pyramid;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RBOActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private RBORenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rbo);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new RBORenderer(this);
        mGLSurfaceView.setRenderer(mGLRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    private class RBORenderer implements GLSurfaceView.Renderer {
        private Context mContext;

        private int mFramebuffer;
        private int mColorRenderBuffer;

        private int mWidth;
        private int mHeight;
        private float mRatio;

        private Pyramid mPyramid;

        private float[] mModelMatrix = new float[16]; // 模型矩阵
        private float[] mViewMatrix = new float[16]; // 相机矩阵
        private float[] mViewModelMatrix = new float[16];
        private float[] mProjectionMatrix = new float[16]; // 投影矩阵
        private float[] mMVPMatrix = new float[16];

        public RBORenderer(Context context) {
            mContext = context;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            // Clears the screen and depth buffer.
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            initData();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;

            GLES30.glViewport(0, 0, mWidth, mHeight);

            mRatio = (float) width / (float) height;
            Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1, 1, 3, 7);
            Matrix.setLookAtM(mViewMatrix, 0,
                    0f, 0f, 5f,
                    0f, 0f, 0f,
                    0f, 1f, 0f);

            initFrameBuffer();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, mFramebuffer);

            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            Matrix.rotateM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);
            Matrix.multiplyMM(mViewModelMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewModelMatrix, 0);

            // 顶点坐标顺序需要调整
            // GLES30.glEnable(GLES30.GL_CULL_FACE);
            // GLES30.glCullFace(GLES30.GL_FRONT);
            // GLES30.glFrontFace(GLES30.GL_CCW);

            mPyramid.draw(gl, mMVPMatrix);

            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);

            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, mFramebuffer);

            GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
            GlUtil.checkGl3Error("glReadBuffer");

            GLES30.glBlitFramebuffer(0, 0, mWidth, mHeight,
                0, 0, mWidth, mHeight,
                GLES30.GL_COLOR_BUFFER_BIT,
                GLES30.GL_NEAREST);
        }

        private void initFrameBuffer() {
            final int[] ids = new int[1];
            GLES30.glGenFramebuffers(1, ids, 0);
            GlUtil.checkGl3Error("glGenFramebuffers");
            mFramebuffer = ids[0];
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);
            GlUtil.checkGl3Error("glBindFramebuffer " + mFramebuffer);

            // Create a color buffer and bind it.
            GLES30.glGenRenderbuffers(1, ids, 0);
            GlUtil.checkGl3Error("glGenRenderbuffers");
            mColorRenderBuffer = ids[0];
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, mColorRenderBuffer);
            GlUtil.checkGl3Error("glBindRenderbuffer " + mColorRenderBuffer);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_RGBA8, mWidth, mHeight);
            GlUtil.checkGl3Error("glRenderbufferStorage");
            GLES30.glFramebufferRenderbuffer(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, mColorRenderBuffer);
            GlUtil.checkGl3Error("glFramebufferRenderbuffer");
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);

            checkFramebufferStatus();

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }

        private void initData() {
            mPyramid = new Pyramid(mContext);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mViewMatrix, 0);
            Matrix.setIdentityM(mViewModelMatrix, 0);
            Matrix.setIdentityM(mProjectionMatrix, 0);
            Matrix.setIdentityM(mMVPMatrix, 0);
        }

        private void checkFramebufferStatus() {
            int status = GLES30.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                String msg = "";
                switch (status) {
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                        msg = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                        break;
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                        msg = "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS";
                        break;
                    case GLES30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                        msg = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                        break;
                    case GLES30.GL_FRAMEBUFFER_UNSUPPORTED:
                        msg = "GL_FRAMEBUFFER_UNSUPPORTED";
                        break;
                }
                throw new RuntimeException(msg + ":" + Integer.toHexString(status));
            }
        }
    }
}
