package com.hiopengl.basic;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MultipleShaderActivity extends ActionBarActivity {
    protected GLSurfaceView mGLSurfaceView;
    protected GLSLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_shader);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new GLSLRenderer(this);
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

    private class GLSLRenderer implements GLSurfaceView.Renderer {
        private Context mContext;

        //渲染程序
        private int mProgram1 = -1;
        private int mProgram2 = -1;

        private FloatBuffer vertex1Buffer;
        private FloatBuffer vertex2Buffer;

        //相机矩阵
        private final float[] mViewMatrix = new float[16];
        //投影矩阵
        private final float[] mProjectMatrix = new float[16];
        //最终变换矩阵
        private final float[] mMVPMatrix1 = new float[16];
        private final float[] mMVPMatrix2 = new float[16];

        private float mRatio = 0.0F;

        private float vertex1[] ={ // x, y, z
                0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };

        private float vertex2[] ={ // x, y, z
                0.5f, -0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f
        };

        public GLSLRenderer(Context context) {
            mContext = context;

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex1.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertex1Buffer = byteBuffer.asFloatBuffer();
            vertex1Buffer.put(vertex1);
            vertex1Buffer.position(0);

            vertex2Buffer = ByteBuffer.allocateDirect(vertex2.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertex2Buffer.put(vertex2);
            vertex2Buffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            //编译顶点着色程序
            String vertexShaderStr1 = ShaderUtil.loadAssets(mContext, "vertex_multiple_shader_1.glsl");
            int vertexShaderId1 = ShaderUtil.compileVertexShader(vertexShaderStr1);
            //编译片段着色程序
            String fragmentShaderStr1 = ShaderUtil.loadAssets(mContext, "fragment_multiple_shader_1.glsl");
            int fragmentShaderId1 = ShaderUtil.compileFragmentShader(fragmentShaderStr1);
            //连接程序
            mProgram1 = ShaderUtil.linkProgram(vertexShaderId1, fragmentShaderId1);

            //编译顶点着色程序
            String vertexShaderStr2 = ShaderUtil.loadAssets(mContext, "vertex_multiple_shader_2.glsl");
            int vertexShaderId2 = ShaderUtil.compileVertexShader(vertexShaderStr2);
            //编译片段着色程序
            String fragmentShaderStr2 = ShaderUtil.loadAssets(mContext, "fragment_multiple_shader_2.glsl");
            int fragmentShaderId2 = ShaderUtil.compileFragmentShader(fragmentShaderStr2);
            //连接程序
            mProgram2 = ShaderUtil.linkProgram(vertexShaderId2, fragmentShaderId2);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);

            mRatio = (float) width / height;

            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix,0,
                    0,0, 3,// 摄像机坐标
                    0f,0f,0f,// 目标物的中心坐标
                    0f,1.0f,0.0f);// 相机方向
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glDepthFunc(GLES30.GL_LESS);

            // 设置透视投影
            Matrix.frustumM(mProjectMatrix,0,
                    -mRatio, mRatio,-1f,1f,
                    1f,333f);

            // 接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
            // 例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
            // 计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix1,0, mProjectMatrix,0, mViewMatrix,0);
            Matrix.multiplyMM(mMVPMatrix2,0, mProjectMatrix,0, mViewMatrix,0);

            GLES30.glUseProgram(mProgram1);
            int uMaxtrixLocation1 = GLES30.glGetUniformLocation(mProgram1,"vMatrix");
            GLES30.glUniformMatrix4fv(uMaxtrixLocation1,1,false, mMVPMatrix1,0);
            int aPositionLocation1 = GLES30.glGetAttribLocation(mProgram1,"vPosition");
            GLES30.glEnableVertexAttribArray(aPositionLocation1);
            GLES30.glVertexAttribPointer(aPositionLocation1,3, GLES30.GL_FLOAT,false,0, vertex1Buffer);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES30.glDisableVertexAttribArray(aPositionLocation1);

            GLES30.glUseProgram(mProgram2);
            int uMaxtrixLocation2 = GLES30.glGetUniformLocation(mProgram2,"vMatrix");
            GLES30.glUniformMatrix4fv(uMaxtrixLocation2,1,false, mMVPMatrix2,0);
            int aPositionLocation2 = GLES30.glGetAttribLocation(mProgram2,"vPosition");
            GLES30.glEnableVertexAttribArray(aPositionLocation2);
            GLES30.glVertexAttribPointer(aPositionLocation2,3, GLES30.GL_FLOAT,false,0, vertex2Buffer);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES30.glDisableVertexAttribArray(aPositionLocation2);
        }
    }
}
