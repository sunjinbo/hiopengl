package com.hiopengl.android.graphics.drawer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class CubeDrawer extends OpenGL3Drawer {

    protected static final float Coords[] ={
            0.5f,  0.5f, 0.5f, // A
            0.5f, 0.5f, -0.5f, // B

            0.5f, 0.5f, -0.5f, // B
            0.5f, -0.5f, -0.5f, // C

            0.5f, -0.5f, -0.5f, // C
            0.5f, -0.5f, 0.5f,  // D

            0.5f, -0.5f, 0.5f,  // D
            0.5f,  0.5f, 0.5f, // A

            -0.5f, 0.5f, 0.5f,  // F
            0.5f,  0.5f, 0.5f, // A

            -0.5f, 0.5f, 0.5f,  // F
            -0.5f, -0.5f, 0.5f,  // E

            -0.5f, -0.5f, 0.5f,  // E
            0.5f, -0.5f, 0.5f,  // D

            -0.5f, 0.5f, -0.5f,  // G
            0.5f, 0.5f, -0.5f, // B

            -0.5f, 0.5f, -0.5f,  // G
            -0.5f, 0.5f, 0.5f,  // F

            -0.5f, 0.5f, -0.5f,  // G
            -0.5f, -0.5f, -0.5f,  // H

            -0.5f, -0.5f, -0.5f,  // H
            -0.5f, -0.5f, 0.5f,  // E

            -0.5f, -0.5f, -0.5f,  // H
            0.5f, -0.5f, -0.5f  // C
    };

    protected static final float Color[] = {
            0.49f, 0.73f, 0.91f, 1.0f, // A
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.77f, 0.12f, 0.23f, 1.0f, // C
            0.77f, 0.12f, 0.23f, 1.0f, // C
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.49f, 0.73f, 0.91f, 1.0f, // A
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.49f, 0.73f, 0.91f, 1.0f, // A
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.0f, 0.1f, 0.0f, 1.0f, // G
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.0f, 0.1f, 0.0f, 1.0f, // G
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.0f, 0.1f, 0.0f, 1.0f, // G
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.77f, 0.12f, 0.23f, 1.0f // C
    };

    private int mPositionVBO;
    private int mColorVBO;
    private int mVAO;

    private FloatBuffer mPositionFloatBuffer;
    private FloatBuffer mColorFloatBuffer;

    private float[] mViewMatrix = new float[16]; // 相机矩阵
    private float[] mProjectionMatrix = new float[16]; // 投影矩阵
    private float[] mMVPMatrix = new float[16]; // 最终变换的矩阵

    public CubeDrawer(Context context) {
        super(context);
        initVertexBuffer();
        initVertexBufferObject();
        initVertexArrayObject();
    }

    @Override
    public String getVertexShaderFile() {
        return "vertex_vertex_opengl_es_30.glsl";
    }

    @Override
    public String getFragmentShaderFile() {
        return "fragment_vertex_opengl_es_30.glsl";
    }

    @Override
    public void setSize(GL10 gl, int width, int height) {
        super.setSize(gl, width, height);
        float ratio = (float) width / (float) height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, 5f,
                0f, 0f, 0f,
                0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void draw(GL10 gl) {
        GLES30.glClearColor(0.2F, 0.2F, 0.2F, 1.0F);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);

        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

        int uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        GLES30.glBindVertexArray(mVAO);

        GLES20.glDrawArrays(GL10.GL_LINES, 0, 24);

        GLES30.glBindVertexArray(0);
    }

    private void initVertexBuffer() {
        ByteBuffer posByteBuffer = ByteBuffer.allocateDirect(Coords.length * 4);
        posByteBuffer.order(ByteOrder.nativeOrder());
        mPositionFloatBuffer = posByteBuffer.asFloatBuffer();
        mPositionFloatBuffer.put(Coords);
        mPositionFloatBuffer.position(0);

        ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(Color.length * 4);
        colorByteBuffer.order(ByteOrder.nativeOrder());
        mColorFloatBuffer = colorByteBuffer.asFloatBuffer();
        mColorFloatBuffer.put(Color);
        mColorFloatBuffer.position(0);
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
