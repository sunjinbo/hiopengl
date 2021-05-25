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
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
        private int mProgram;

        private int mFramebuffer;
        private int mColorRenderBuffer;
        private int mDepthRenderBuffer;

        private int mWidth;
        private int mHeight;

        private Pyramid mPyramid;

        private float[] mViewMatrix = new float[16]; // 相机矩阵
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
            GLES30.glViewport(0, 0, width, height);
            mWidth = width;
            mHeight = height;

            float ratio = (float) width / (float) height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
            Matrix.setLookAtM(mViewMatrix, 0,
                    0f, 0f, 5f,
                    0f, 0f, 0f,
                    0f, 1f, 0f);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            initFrameBuffer();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

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

        private void initProgram() {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_rbo.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            GlUtil.checkGl3Error("Check Vertex Shader!");
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_rbo.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            GlUtil.checkGl3Error("Check Fragment Shader!");
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            GlUtil.checkGl3Error("Check GL Program!");

            GLES30.glUseProgram(mProgram);
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
