package com.hiopengl.glsl;

import android.content.Context;
import android.opengl.GLES30;

import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class BaseProgram {

    private int mProgramId;

    private FloatBuffer mCoordBuffer;
    private FloatBuffer mColorBuffer;

    public abstract float[] getCoordArray();
    public abstract float[] getColorArray();

    protected BaseProgram(Context context, String vertexShaderFileName, String fragmentShaderFileName) {
        // 加载顶点数据
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(getCoordArray().length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mCoordBuffer = byteBuffer.asFloatBuffer();
        mCoordBuffer.put(getCoordArray());
        mCoordBuffer.position(0);

        mColorBuffer = ByteBuffer.allocateDirect(getColorArray().length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mColorBuffer.put(getColorArray());
        mColorBuffer.position(0);

        // 编译顶点着色程序
        final String vertexShader = ShaderUtil.loadAssets(context, vertexShaderFileName);
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        // 编译片段着色程序
        final String fragmentShader = ShaderUtil.loadAssets(context, fragmentShaderFileName);
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        // 链接程序
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        // 在OpenGL ES环境中使用该程序
        GLES30.glUseProgram(mProgramId);
    }

    public void draw() {
        // 在OpenGL ES环境中使用该程序
        GLES30.glUseProgram(mProgramId);

        int aPositionLocation = GLES30.glGetAttribLocation(mProgramId,"vPosition");
        GLES30.glEnableVertexAttribArray(aPositionLocation);
        // x y z 所以数据size是3
        GLES30.glVertexAttribPointer(aPositionLocation,3,GLES30.GL_FLOAT,false,0, mCoordBuffer);

        int aColorLocation = GLES30.glGetAttribLocation(mProgramId,"aColor");
        // 准备颜色数据RGBA，所以数据size是 4
        GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT, false, 0, mColorBuffer);
        // 启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(aColorLocation);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        // 禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(aColorLocation);
    }
}
