package com.hiopengl.basic.vertex;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Bundle;

import com.hiopengl.utils.ShaderUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VertexBufferOpenGL30Activity extends VertexActivity {

    private int mProgramId;
    private int mPositionVBO;
    private int mColorVBO;
    private int mVAO;

    private float[] mViewMatrix = new float[16]; // 相机矩阵
    private float[] mProjectionMatrix = new float[16]; // 投影矩阵
    private float[] mMVPMatrix = new float[16]; // 最终变换的矩阵

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShaderUtil.setEGLContextClientVersion(3);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initVertexBuffer();
        initShaderProgram();
        initVertexBufferObject();
        initVertexArrayObject(); // 将创建的vbo对象关联到vao上
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
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
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(mProgramId);

        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

        int uMatrixLocation = GLES20.glGetUniformLocation(mProgramId, "uMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        GLES30.glBindVertexArray(mVAO);

        GLES20.glDrawArrays(GL10.GL_LINES, 0, 24);

        GLES30.glBindVertexArray(0);
    }

    private void initShaderProgram() {
        // 编译顶点着色程序
        final String vertexShader = ShaderUtil.loadAssets(this, "vertex_vertex_opengl_es_30.glsl");
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        // 编译片段着色程序
        final String fragmentShader = ShaderUtil.loadAssets(this, "fragment_vertex_opengl_es_30.glsl");
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        // 链接程序
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        // 在OpenGL ES环境中使用该程序
        GLES30.glUseProgram(mProgramId);
    }

    private void initVertexBufferObject() {
        // 创建位置坐标数据的VBO对象并绑定到缓冲区
        int[] posBuffers = new int[1];
        GLES30.glGenBuffers(posBuffers.length, posBuffers, 0);

        mPositionVBO = posBuffers[0];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mPositionVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mPositionFloatBuffer.capacity() * 4, mPositionFloatBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // 创建颜色数据的VBO对象并绑定到缓冲区
        int[] colorBuffers = new int[1];
        GLES30.glGenBuffers(colorBuffers.length, colorBuffers, 0);

        mColorVBO = colorBuffers[0];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mColorFloatBuffer.capacity() * 4, mColorFloatBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private void initVertexArrayObject() {
        int[] buffer = new int[1];
        GLES30.glGenBuffers(buffer.length, buffer, 0);

        mVAO = buffer[0];

        GLES30.glBindVertexArray(mVAO);

        // 关联位置数据
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mPositionVBO);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // 关联颜色数据
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVBO);
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindVertexArray(0);
    }
}
