package com.hiopengl.android.graphics.drawer;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class ExternalOESTextureDrawer extends OpenGL3Drawer {

    private int mTextureId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textBuffer;

    //3个定点，等腰直角
    float triangleCoords[] = {
            -1f, -1f, 0.0f,  // bottom left
            1f,  1f, 0.0f, // top
            1f, -1f, 0.0f, // bottom right
            1f,  1f, 0.0f, // top
            -1f, 1f, 0.0f  // top left
    };

    float textCoords[] ={
            0.0f,  0.0f, // bottom left
            1.0f, 1.0f, // top
            1.0f, 0.0f,  // bottom right
            1.0f,  1.0f, // top
            0.0f, 1.0f // top left
    };

    public ExternalOESTextureDrawer(Context context, int textureId) {
        super(context);
        mTextureId = textureId;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(textCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textBuffer = byteBuffer.asFloatBuffer();
        textBuffer.put(textCoords);
        textBuffer.position(0);
    }


    @Override
    public String getVertexShaderFile() {
        return "vertex_texture_2d.glsl";
    }

    @Override
    public String getFragmentShaderFile() {
        return "fragment_oes_texture_2d.glsl";
    }

    @Override
    public void draw(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(mProgram);

        GLES30.glVertexAttribPointer(0,3, GLES30.GL_FLOAT,false,0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textBuffer);
        GLES30.glEnableVertexAttribArray(1);

        // 设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        // 将纹理单元传递片段着色器的u_TextureUnit
        int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
        GLES30.glUniform1i(uTextureLocation, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 5);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }
}