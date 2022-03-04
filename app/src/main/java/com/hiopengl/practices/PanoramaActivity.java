package com.hiopengl.practices;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.BitmapUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PanoramaActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(this);
        initBufferData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
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
        final String vertexShader = ShaderUtil.loadAssets(this, "vertex_panorama.glsl");
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        // 编译片段着色程序
        final String fragmentShader = ShaderUtil.loadAssets(this, "fragment_panorama.glsl");
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        // 链接程序
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        // 在OpenGL ES环境中使用该程序
        GLES32.glUseProgram(mProgramId);

        mVertexAttrib = GLES30.glGetAttribLocation(mProgramId, "position");
        mTexCoordsAttrib = GLES30.glGetAttribLocation(mProgramId, "inTexcoord");

        // 加载纹理
        final int[] textureObjectIds = new int[1];
        GLES30.glGenTextures(1, textureObjectIds, 0);
        mTextureId = textureObjectIds[0];

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        Bitmap bitmap = BitmapUtil.getBitmapFromAssets(this, "panorama.jpg");
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        BitmapUtil.recycleBitmap(bitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        GLES30.glViewport(0, 0, w, h);

        mWidth = w;
        mHeight = h;

        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,
                0,0, 0,// 摄像机坐标
                0f,0f,-4f,// 目标物的中心坐标
                0f,1.0f,0.0f);// 相机方向

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewModelMatrix, 0);

        final float ratio = (float) mWidth / mHeight;
        Matrix.frustumM(mProjectMatrix,0, -ratio, ratio,-1f,1f, 1f,333f);
        Matrix.multiplyMM(mViewModelMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix,0, mProjectMatrix,0, mViewModelMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES30.glFrontFace(GLES30.GL_CW);
        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glEnable(GLES30.GL_CULL_FACE);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LESS);

        GLES30.glClearColor(0.f, 0.f, 0.2f, .5f);
        GLES30.glClearDepthf(1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_STENCIL_BUFFER_BIT);

        GLES30.glUseProgram(mProgramId);

        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.0f, 0.2f, 0.0f);
        int uMatrixLocation = GLES30.glGetUniformLocation(mProgramId,"vMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation,1,false, mMVPMatrix,0);

        GLES30.glEnableVertexAttribArray(mVertexAttrib);
        GLES30.glVertexAttribPointer (mVertexAttrib, 3, GLES30.GL_FLOAT, false, 0, mVertexBuffer);

        GLES30.glEnableVertexAttribArray(mTexCoordsAttrib);
        GLES30.glVertexAttribPointer (mTexCoordsAttrib, 2, GLES30.GL_FLOAT, false, 0, mTexCoordsBuffer);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgramId, "tex_yuv"), 0);

        // 绘制顶点
        int numIndices = 2 * SegmentsW * (SegmentsH - 1) * 3;
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, numIndices, GLES30.GL_UNSIGNED_SHORT, mIndicesBuffer);

        // 禁止顶点数组的句柄
        GLES30.glBindVertexArray(0);
        GLES30.glDisableVertexAttribArray(mVertexAttrib);
        GLES30.glDisableVertexAttribArray(mTexCoordsAttrib);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    private void initBufferData() {
        int numVertices = (SegmentsW + 1) * (SegmentsH + 1);
        int numIndices = 2 * SegmentsW * (SegmentsH - 1) * 3;

        float[] vertices = new float[numVertices * 3];
        float[] normals = new float[numVertices * 3];
        short[] indices = new short[numIndices];

        int i, j;
        int vertIndex = 0, index = 0;
        float normLen = 1.0f / Radius;

        for (j = 0; j <= SegmentsH; ++j) {
            float horAngle = (float) (Math.PI * j / SegmentsH);
            float z = Radius * (float) Math.cos(horAngle);
            float ringRadius = Radius * (float) Math.sin(horAngle);

            for (i = 0; i <= SegmentsW; ++i) {
                float verAngle = (float) (2.0f * Math.PI * i / SegmentsW);
                float x = ringRadius * (float) Math.cos(verAngle);
                float y = ringRadius * (float) Math.sin(verAngle);

                normals[vertIndex] = x * normLen;
                vertices[vertIndex++] = x;
                normals[vertIndex] = z * normLen;
                vertices[vertIndex++] = z;
                normals[vertIndex] = y * normLen;
                vertices[vertIndex++] = y;

                if(indices.length==0) continue;

                if (i > 0 && j > 0) {
                    short a = (short) ((SegmentsW + 1) * j + i);
                    short b = (short) ((SegmentsW + 1) * j + i - 1);
                    short c = (short) ((SegmentsW + 1) * (j - 1) + i - 1);
                    short d = (short) ((SegmentsW + 1) * (j - 1) + i);

                    if (j == SegmentsH) {
                        indices[index++] = a;
                        indices[index++] = c;
                        indices[index++] = d;
                    } else if (j == 1) {
                        indices[index++] = a;
                        indices[index++] = b;
                        indices[index++] = c;
                    } else {
                        indices[index++] = a;
                        indices[index++] = b;
                        indices[index++] = c;
                        indices[index++] = a;
                        indices[index++] = c;
                        indices[index++] = d;
                    }
                }
            }
        }



        int numUvs = (SegmentsH + 1) * (SegmentsW + 1) * 2;
        float[] textureCoords = new float[numUvs];

        numUvs = 0;
        for (j = 0; j <= SegmentsH; ++j) {
            for (i = SegmentsW; i >= 0; --i) {
                float u = (float) i / SegmentsW;
                textureCoords[numUvs++] = MirrorTextureCoords ? 1.0f - u : u;
                textureCoords[numUvs++] = (float) j / SegmentsH;
            }
        }

        mVertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

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
}
