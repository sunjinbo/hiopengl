package com.hiopengl.advanced.model;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public abstract class Object3D {

    protected int mProgram = -1;

    // 模型矩阵
    protected final float[] mModelMatrix = new float[16];

    // 顶点数据缓冲区
    protected FloatBuffer mVertexBuffer; // 坐标点
    protected FloatBuffer mBaryBuffer; // 重心坐标的坐标

    protected int mNumVertices = 0;

    // 上下文
    protected Context mContext;

    protected Object3D(Context context) {
        mContext = context;
        initProgram();
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public abstract Mesh getType();

    public void draw(GL10 gl, float[] mvpMatrix) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);

        GLES30.glUseProgram(mProgram);

        int uMatrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation,1,false, mvpMatrix,0);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,0, mVertexBuffer);

        int baryLocation = GLES30.glGetAttribLocation(mProgram,"vBary");
        GLES30.glEnableVertexAttribArray(baryLocation);
        GLES30.glVertexAttribPointer(baryLocation,3, GLES30.GL_FLOAT,false,0, mBaryBuffer);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mNumVertices);

        GLES30.glDisableVertexAttribArray(baryLocation);
        GLES30.glDisableVertexAttribArray(aPositionLocation);
    }

    public float[] getModelMatrix() {
        return mModelMatrix;
    }

    protected void initProgram() {
        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_mesh.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_mesh.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);
    }

    protected void setData(float[] vertices, short[] indices) {
        float[] vt = new float[indices.length * 3];
        float[] bc = new float[indices.length * 3];

        for (int i = 0; i < indices.length; ++i) {
            short ix = indices[i];
            vt[i * 3 + 0] = vertices[ix * 3];
            vt[i * 3 + 1] = vertices[ix * 3 + 1];
            vt[i * 3 + 2] = vertices[ix * 3 + 2];
        }

        for (int i = 0; i < bc.length; ++i) {
            if (i % 9 == 0 || i % 9 == 4 || i % 9 == 8) {
                bc[i] = 1f;
            } else {
                bc[i] = 0f;
            }
        }

        setData(vt, bc);
    }

    protected void setData(float[] vertices, float[] barycentrics) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        mNumVertices = vertices.length / 3;

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(barycentrics.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        mBaryBuffer = byteBuffer2.asFloatBuffer();
        mBaryBuffer.put(barycentrics);
        mBaryBuffer.position(0);
    }
}
