package com.hiopengl.android.recorder;

import android.content.Context;
import android.opengl.GLES30;

import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class TextureDrawer {

    private int mProgram;
    private int mWidth, mHeight;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textBuffer;

    float vertexCoords[] = {
            -1f, 1f, 0.0f,  // top left
            1f,  1f, 0.0f, // top right
            -1f, -1f, 0.0f, // bottom left

            1f, 1f, 0.0f,  // top right
            1f,  -1f, 0.0f, // bottom right
            -1f, -1f, 0.0f  // bottom left
    };

    float textCoords[] ={
            0.0f,  1.0f, // top left
            1.0f, 1.0f, // top right
            0.0f, 0.0f,  // bottom left

            1.0f,  1.0f, // top right
            1.0f, 0.0f, // bottom right
            0.0f, 0.0f // bottom left
    };

    public TextureDrawer(Context context, int width, int height) {
        mWidth = width;
        mHeight = height;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(textCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textBuffer = byteBuffer.asFloatBuffer();
        textBuffer.put(textCoords);
        textBuffer.position(0);

        ShaderUtil.setEGLContextClientVersion(3);

        GlUtil.checkGlError("Check gl status.");

        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(context, "vertex_fbo_blit_x2.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(context, "fragment_fbo_blit_x2.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        GLES30.glViewport(0, 0, mWidth, mHeight);
    }

    public void drawTexture(int textureId) {
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
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);

        // 将纹理单元传递片段着色器的u_TextureUnit
        int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
        GLES30.glUniform1i(uTextureLocation, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }
}
