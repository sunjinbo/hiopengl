package com.hiopengl.basic.texture;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Texture2DActivity extends ActionBarActivity {
    protected GLSurfaceView mGLSurfaceView;
    protected Texture2DRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_2d);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new Texture2DRenderer(this);
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

    protected class Texture2DRenderer implements GLSurfaceView.Renderer {
        protected Context mContext;

        //渲染程序
        protected int mProgram = -1;
        private FloatBuffer vertexBuffer;
        private FloatBuffer textBuffer;
        private FloatBuffer colorBuffer;

        //3个定点，等腰直角
        float triangleCoords[] ={
                0.5f,  0.5f, 0.0f, // top
                0.5f, -0.5f, 0.0f, // bottom left
                -0.5f, -0.5f, 0.0f  // bottom right
        };

        float textCoords[] ={
                1.0f,  1.0f, // top
                1.0f, 0.0f, // bottom left
                0.0f, 0.0f  // bottom right
        };

        private float color[] = {
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f
        };

        // 纹理
        private int textureId;

        public Texture2DRenderer(Context context) {
            mContext = context;
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

            colorBuffer = ByteBuffer.allocateDirect(color.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            colorBuffer.put(color);
            colorBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_texture_2d.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_texture_2d.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgram);

            textureId = GlUtil.loadTexture(mContext, R.drawable.texture);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glVertexAttribPointer(0,3, GLES30.GL_FLOAT,false,0, vertexBuffer);
            GLES30.glEnableVertexAttribArray(0);

            GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
            GLES30.glEnableVertexAttribArray(1);

            GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 0, textBuffer);
            GLES30.glEnableVertexAttribArray(2);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
            GLES30.glUniform1i(uTextureLocation, 0);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

            //禁止顶点数组的句柄
            GLES30.glDisableVertexAttribArray(0);
            GLES30.glDisableVertexAttribArray(1);
            GLES30.glDisableVertexAttribArray(2);
        }
    }
}