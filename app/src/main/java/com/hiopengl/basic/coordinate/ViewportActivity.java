package com.hiopengl.basic.coordinate;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewportActivity extends ActionBarActivity {
    protected GLSurfaceView mGLSurfaceView;
    protected ViewportRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_glsurfaceview);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new ViewportRenderer(this);
        mGLSurfaceView.setRenderer(mGLRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        mGLRenderer.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mGLRenderer.onResume();
    }

    protected class ViewportRenderer implements GLSurfaceView.Renderer, Runnable {
        protected Context mContext;

        //渲染程序
        protected int mProgram = -1;
        private FloatBuffer vertexBuffer;
        private FloatBuffer colorBuffer;

        // 立方体顶点
        float triangleCoords[] ={
                0.5f,  0.5f, 0.5f, // A
                0.5f, 0.5f, -0.5f, // B

                0.5f, 0.5f, -0.5f, // B
                0.5f, -0.5f, -0.5f,  // C

                0.5f, -0.5f, -0.5f,  // C
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

        private float color[] = {
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f
        };

        //相机矩阵
        private final float[] mViewMatrix = new float[16];
        //投影矩阵
        private final float[] mProjectMatrix = new float[16];
        //最终变换矩阵
        private final float[] mMVPMatrix = new float[16];

        private int mWidth = 0;
        private int mHeight = 0;
        private float mHorizontalStep = 0;
        private float mVerticalStep = 0;

        private int mHorizontalSteps = 0;
        private int mVerticalSteps = 0;

        private boolean mReverse = true;
        private boolean mRunning = false;

        public ViewportRenderer(Context context) {
            mContext = context;
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length*4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuffer.asFloatBuffer();
            //把这门语法() 推送给GPU
            vertexBuffer.put(triangleCoords);
            vertexBuffer.position(0);

            colorBuffer = ByteBuffer.allocateDirect(color.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            //传入指定的数据
            colorBuffer.put(color);
            colorBuffer.position(0);
        }

        public void onResume() {
            mRunning = true;
            new Thread(this).start();
        }

        public void onPause() {
            mRunning = false;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_viewmodel.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_viewmodel.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgram);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mWidth = width;
            mHeight = height;

            mHorizontalStep = (mWidth / 4) / 50;
            mVerticalStep = (mHeight / 4) / 50;

            GLES30.glViewport(0, 0, mWidth, mHeight);

            float ratio = (float) width / height;

            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix,0,
                    0,0, 3,// 摄像机坐标
                    0f,0f,0f,// 目标物的中心坐标
                    0f,1.0f,0.0f);// 相机方向

            // 设置透视投影
            Matrix.frustumM(mProjectMatrix,0,
                    -ratio, ratio,-1f,1f,
                    1f,333f);

            // 接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
            // 例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
            // 计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix,0, mProjectMatrix,0, mViewMatrix,0);

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glViewport(mHorizontalSteps, mVerticalSteps,
                    mWidth - mHorizontalSteps * 2, mHeight - mVerticalSteps * 2);

            int uMaxtrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
            GLES30.glUniformMatrix4fv(uMaxtrixLocation,1,false, mMVPMatrix,0);

            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            //x y z 所以数据size 是3
            GLES30.glVertexAttribPointer(aPositionLocation,3,GLES30.GL_FLOAT,false,0,vertexBuffer);

            int aColorLocation = GLES30.glGetAttribLocation(mProgram,"aColor");
            //准备颜色数据 rgba 所以数据size是 4
            GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
            //启用顶点颜色句柄
            GLES30.glEnableVertexAttribArray(aColorLocation);

            GLES30.glDrawArrays(GLES30.GL_LINES, 0, 24);

            //禁止顶点数组的句柄
            GLES30.glDisableVertexAttribArray(aPositionLocation);
            GLES30.glDisableVertexAttribArray(aColorLocation);
        }

        @Override
        public void run() {
            while (mRunning) {
                SystemClock.sleep(111);
                if (mReverse) {
                    mHorizontalSteps -= mHorizontalStep;
                    mVerticalSteps -= mVerticalStep;
                    if (mHorizontalSteps <= 0 || mVerticalSteps <= 0) {
                        mHorizontalSteps = 0;
                        mVerticalSteps = 0;
                        mReverse = false;
                    }
                } else {
                    mHorizontalSteps += mHorizontalStep;
                    mVerticalSteps += mVerticalStep;
                    if (mHorizontalSteps >= mWidth / 4 || mVerticalSteps >= mHeight / 4) {
                        mHorizontalSteps = mWidth / 4;
                        mVerticalSteps = mHeight / 4;
                        mReverse = true;
                    }
                }
            }
        }
    }
}