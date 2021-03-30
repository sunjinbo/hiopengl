package com.hiopengl.basic.vertex;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;

import com.hiopengl.utils.ShaderUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VertexArrayOpenGL20Activity extends VertexActivity {

    private int mProgramId;

    private float[] mViewMatrix = new float[16]; // 相机矩阵
    private float[] mProjectionMatrix = new float[16]; // 投影矩阵
    private float[] mMVPMatrix = new float[16]; // 最终变换的矩阵

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShaderUtil.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initVertexBuffer();
        initShaderProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / (float) height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, 5f,
                0f, 0f, 0f,
                0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);

        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

        int uMatrixLocation = GLES20.glGetUniformLocation(mProgramId, "uMatrix");
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        int aPositionLocation = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, mPositionFloatBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        int aColorLocation = GLES20.glGetAttribLocation(mProgramId, "aColor");
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, mColorFloatBuffer);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        GLES20.glDrawArrays(GL10.GL_LINES, 0, 24);

        GLES20.glDisableVertexAttribArray(aColorLocation);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
    }

    private void initShaderProgram() {
        // 编译顶点着色程序
        final String vertexShader = ShaderUtil.loadAssets(this, "vertex_vertex_opengl_es_20.glsl");
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        // 编译片段着色程序
        final String fragmentShader = ShaderUtil.loadAssets(this, "fragment_vertex_opengl_es_20.glsl");
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        // 链接程序
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        // 在OpenGL ES环境中使用该程序
        GLES20.glUseProgram(mProgramId);
    }
}