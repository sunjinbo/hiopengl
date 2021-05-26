package com.hiopengl.android.graphics.drawer;

import android.content.Context;
import android.opengl.GLES30;

import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import javax.microedition.khronos.opengles.GL10;

public abstract class OpenGL3Drawer {

    protected Context mContext;
    protected int mProgram;

    protected OpenGL3Drawer(Context context) {
        mContext = context;
        initProgram();
    }

    public abstract String getVertexShaderFile();
    public abstract String getFragmentShaderFile();
    public abstract void draw(GL10 gl);

    public void setSize(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    private void initProgram() {
        ShaderUtil.setEGLContextClientVersion(3);
        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(mContext, getVertexShaderFile());
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        GlUtil.checkGl3Error("Check Vertex Shader!");
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(mContext, getFragmentShaderFile());
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        GlUtil.checkGl3Error("Check Fragment Shader!");
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        GlUtil.checkGl3Error("Check GL Program!");
        //使用程序
        GLES30.glUseProgram(mProgram);
        GlUtil.checkGl3Error("Check USE Program!");
    }
}
