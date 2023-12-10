package com.hiopengl.practices;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.BitmapUtil;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.MDQuaternion;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PanoramaActivity extends ActionBarActivity implements GLSurfaceView.Renderer, Runnable {
    private static final float PI = 3.1415926f;
    private static final int SegmentsW = 50;
    private static final int SegmentsH = 50;
    private static final float Radius = 2.0f;
    private static final boolean MirrorTextureCoords = true;

    private GLSurfaceView mGLSurfaceView;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private FloatBuffer mTexCoordsBuffer;
    private ShortBuffer mIndicesBuffer;

    private int mVertexAttrib;
    private int mTexCoordsAttrib;

    private int mProgramId;
    private int mTextureId;

    private int mWidth;
    private int mHeight;

    private final float[] mModelMatrix = new float[16];
    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //ViewModel矩阵
    private final float[] mViewModelMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    static float vertices[] = new float[]{
        -0.6081204068324845f, 0.6081204068324845f, 0.510273594836914f,
        0.0f, 0.766044451956441f, 0.6427875991544609f,
        0.6081204068324845f, 0.6081204068324845f, 0.510273594836914f,
        -0.766044451956441f, 0.0f, 0.6427875991544609f,
        0.0f, 0.0f, 1.0f,
        0.766044451956441f, 0.0f, 0.6427875991544609f,
        -0.6081204068324845f, -0.6081204068324845f, 0.510273594836914f,
        0.0f, -0.766044451956441f, 0.6427875991544609f,
        0.6081204068324845f, -0.6081204068324845f, 0.510273594836914f
    };

    static float verticesColor[] = new float[]{
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    static short indices[] = new short[]{
        0, 1, 3,
        1, 4, 3,
        1, 2, 4,
        2, 5, 4,
        3, 4, 6,
        4, 7, 6,
        4, 5, 7,
        5, 8, 7
    };

    static float[] textureCoords = {
        0.0f, 1.0f,
        0.5f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.5f,
        0.5f, 0.5f,
        1.0f, 0.5f,
        0.0f, 0.0f,
        0.5f, 0.0f,
        1.0f, 0.0f
    };

    private float mAngle = 0;
    private boolean mRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(this);

//        for (int i = 0; i < textureCoords.length; ++i) {
//            if ((i + 1) % 2 == 0) {
//                textureCoords[i] = textureCoords[i] * 2;
//            }
//        }

        initBufferData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mRunning = true;
        new Thread(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        ShaderUtil.setEGLContextClientVersion(3);
        // 编译顶点着色程序
        final String vertexShader = ShaderUtil.loadAssets(this, "vertex_panorama_erp.glsl");
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        GlUtil.checkGl3Error("vertexShaderId");
        // 编译片段着色程序
        final String fragmentShader = ShaderUtil.loadAssets(this, "fragment_panorama_erp.glsl");
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        GlUtil.checkGl3Error("fragmentShaderId");
        // 链接程序
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        // 在OpenGL ES环境中使用该程序
        GLES32.glUseProgram(mProgramId);

        mVertexAttrib = GLES30.glGetAttribLocation(mProgramId, "position");
        mTexCoordsAttrib = GLES30.glGetAttribLocation(mProgramId, "inColor");

        // 加载纹理
//        final int[] textureObjectIds = new int[1];
//        GLES30.glGenTextures(1, textureObjectIds, 0);
//        mTextureId = textureObjectIds[0];
//
//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//        Bitmap bitmap = BitmapUtil.getBitmapFromAssets(this, "number.jpg");
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
//        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
//        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
//        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
//        BitmapUtil.recycleBitmap(bitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        GLES30.glViewport(0, 0, w, h);

        mWidth = w;
        mHeight = h;

        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.rotateM(mModelMatrix, 0, -90f, 0.5f, 1f, 0.5f); // 旋转一个角度

        Log.e("sunjinbo", "# Model Matrix #");
        Log.e("sunjinbo", mModelMatrix[0] + " " + mModelMatrix[1] + " " + mModelMatrix[2] + " " + mModelMatrix[3]);
        Log.e("sunjinbo", mModelMatrix[4] + " " + mModelMatrix[5] + " " + mModelMatrix[6] + " " + mModelMatrix[7]);
        Log.e("sunjinbo", mModelMatrix[8] + " " + mModelMatrix[9] + " " + mModelMatrix[10] + " " + mModelMatrix[11]);
        Log.e("sunjinbo", mModelMatrix[12] + " " + mModelMatrix[13] + " " + mModelMatrix[14] + " " + mModelMatrix[15]);

        MDQuaternion quaternion = new MDQuaternion();
        quaternion.setFromAxis(0.5f, 1f, 0.5f, 90f); // 同步旋转一个角度
        float[] dot = new float[] {0f,0f,1f}; // 目标物的中心坐标
//        dot = quaternion.rotateVec(dot);

        float[] up = new float[] {0f,1.0f,0.0f}; // 相机方向
//        up = quaternion.rotateVec(up);

        Matrix.setLookAtM(mViewMatrix,0,
            0,0, 0f,// 摄像机坐标
            dot[0], dot[1], dot[2],// 目标物的中心坐标
            up[0], up[1], up[2]);// 相机方向

        Log.e("sunjinbo", "# View Matrix #");
        Log.e("sunjinbo", mViewMatrix[0] + " " + mViewMatrix[1] + " " + mViewMatrix[2] + " " + mViewMatrix[3]);
        Log.e("sunjinbo", mViewMatrix[4] + " " + mViewMatrix[5] + " " + mViewMatrix[6] + " " + mViewMatrix[7]);
        Log.e("sunjinbo", mViewMatrix[8] + " " + mViewMatrix[9] + " " + mViewMatrix[10] + " " + mViewMatrix[11]);
        Log.e("sunjinbo", mViewMatrix[12] + " " + mViewMatrix[13] + " " + mViewMatrix[14] + " " + mViewMatrix[15]);

        Matrix.multiplyMM(mViewModelMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        Log.e("sunjinbo", "# View Model Matrix #");
        Log.e("sunjinbo", mViewModelMatrix[0] + " " + mViewModelMatrix[1] + " " + mViewModelMatrix[2] + " " + mViewModelMatrix[3]);
        Log.e("sunjinbo", mViewModelMatrix[4] + " " + mViewModelMatrix[5] + " " + mViewModelMatrix[6] + " " + mViewModelMatrix[7]);
        Log.e("sunjinbo", mViewModelMatrix[8] + " " + mViewModelMatrix[9] + " " + mViewModelMatrix[10] + " " + mViewModelMatrix[11]);
        Log.e("sunjinbo", mViewModelMatrix[12] + " " + mViewModelMatrix[13] + " " + mViewModelMatrix[14] + " " + mViewModelMatrix[15]);

        float ratio = (float) mWidth / mHeight;
        Log.e("sunjinbo", "ratio = " + ratio);

        Matrix.frustumM(mProjectMatrix,0, -ratio, ratio,-1f,1f,0.5f,100f); // 设置透视投影

        Log.e("sunjinbo", "# Projection Matrix #");
        Log.e("sunjinbo", mProjectMatrix[0] + " " + mProjectMatrix[1] + " " + mProjectMatrix[2] + " " + mProjectMatrix[3]);
        Log.e("sunjinbo", mProjectMatrix[4] + " " + mProjectMatrix[5] + " " + mProjectMatrix[6] + " " + mProjectMatrix[7]);
        Log.e("sunjinbo", mProjectMatrix[8] + " " + mProjectMatrix[9] + " " + mProjectMatrix[10] + " " + mProjectMatrix[11]);
        Log.e("sunjinbo", mProjectMatrix[12] + " " + mProjectMatrix[13] + " " + mProjectMatrix[14] + " " + mProjectMatrix[15]);

        Matrix.multiplyMM(mMVPMatrix,0, mProjectMatrix,0, mViewModelMatrix,0);
//        Matrix.setIdentityM(mMVPMatrix, 0);

        Log.e("sunjinbo", "# MVP Matrix #");
        Log.e("sunjinbo", mMVPMatrix[0] + " " + mMVPMatrix[1] + " " + mMVPMatrix[2] + " " + mMVPMatrix[3]);
        Log.e("sunjinbo", mMVPMatrix[4] + " " + mMVPMatrix[5] + " " + mMVPMatrix[6] + " " + mMVPMatrix[7]);
        Log.e("sunjinbo", mMVPMatrix[8] + " " + mMVPMatrix[9] + " " + mMVPMatrix[10] + " " + mMVPMatrix[11]);
        Log.e("sunjinbo", mMVPMatrix[12] + " " + mMVPMatrix[13] + " " + mMVPMatrix[14] + " " + mMVPMatrix[15]);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES30.glFrontFace(GLES30.GL_CW);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);

        GLES30.glClearColor(0.f, 0.f, 0.2f, .5f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_STENCIL_BUFFER_BIT);

        GLES30.glUseProgram(mProgramId);

        int uMatrixLocation = GLES30.glGetUniformLocation(mProgramId,"vMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation,1,false, mMVPMatrix,0);

        GLES30.glEnableVertexAttribArray(mVertexAttrib);
        GLES30.glVertexAttribPointer (mVertexAttrib, 3, GLES30.GL_FLOAT, false, 0, mVertexBuffer);

        GLES30.glEnableVertexAttribArray(mTexCoordsAttrib);
        GLES30.glVertexAttribPointer (mTexCoordsAttrib, 3, GLES30.GL_FLOAT, false, 0, mColorBuffer);

//        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
//        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgramId, "tex_yuv"), 0);

        // 绘制顶点
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_SHORT, mIndicesBuffer);

        // 禁止顶点数组的句柄
        GLES30.glBindVertexArray(0);
        GLES30.glDisableVertexAttribArray(mVertexAttrib);
        GLES30.glDisableVertexAttribArray(mTexCoordsAttrib);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    private void initBufferData() {

        mVertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        mColorBuffer = ByteBuffer.allocateDirect(verticesColor.length * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();
        mColorBuffer.put(verticesColor);
        mColorBuffer.position(0);

        mTexCoordsBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexCoordsBuffer.put(textureCoords);
        mTexCoordsBuffer.position(0);

        mIndicesBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        mIndicesBuffer.put(indices);
        mIndicesBuffer.position(0);
    }

    @Override
    public void run() {
        while (mRunning) {
            SystemClock.sleep(111);
            mAngle += 0.1;
            if (mAngle >= 360) mAngle = 0;
        }
    }
}
