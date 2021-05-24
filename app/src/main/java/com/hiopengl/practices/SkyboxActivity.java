package com.hiopengl.practices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLDebugHelper;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.LogUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SkyboxActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private SkyboxRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skybox);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new SkyboxRenderer(this);
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

    private class SkyboxRenderer implements GLSurfaceView.Renderer {

        private Context mContext;

        private int mProgram;
        private int mCubeTexture = 0;

        private int skyboxTextures[] = {
                R.drawable.right,
                R.drawable.left,
                R.drawable.top,
                R.drawable.bottom,
                R.drawable.back,
                R.drawable.front
        };

        private float skyboxVertices[] = {
                // Positions
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };

        private FloatBuffer mVertexBuffer;

        private float[] mViewMatrix = new float[16]; // 相机矩阵
        private float[] mProjectionMatrix = new float[16]; // 投影矩阵
        private float[] mMVPMatrix = new float[16]; // 最终变换的矩阵

        public SkyboxRenderer(Context context) {
            mContext = context;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);
            initProgram();
            initData();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);

            float ratio = (float) width / (float) height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
            Matrix.setLookAtM(mViewMatrix, 0,
                    0f, 0f, 5f,
                    0f, 0f, 0f,
                    0f, 1f, 0f);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glUseProgram(mProgram);

            GLES30.glDepthFunc(GLES30.GL_LEQUAL);

            Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

            int uMatrixLocation = GLES30.glGetUniformLocation(mProgram,"mvp");
            GLES30.glUniformMatrix4fv(uMatrixLocation,1,false, mMVPMatrix,0);

            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"position");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,0, mVertexBuffer);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, mCubeTexture);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"skybox");
            GLES30.glUniform1i(uTextureLocation, 0);

            GLES20.glDrawArrays(GL10.GL_TRIANGLES, 0, 36);

            GLES30.glDisableVertexAttribArray(aPositionLocation);

            GLES30.glDepthFunc(GLES30.GL_LESS);
        }

        private void initProgram() {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_skybox.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            GlUtil.checkGl3Error("Check Vertex Shader!");
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_skybox.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            GlUtil.checkGl3Error("Check Fragment Shader!");
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            GlUtil.checkGl3Error("Check GL Program!");
        }

        private void initData() {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(skyboxVertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer = byteBuffer.asFloatBuffer();
            mVertexBuffer.put(skyboxVertices);
            mVertexBuffer.position(0);

            List<Bitmap> faces = new ArrayList<>();
            for (int i = 0; i < skyboxTextures.length; ++i) {
                faces.add(BitmapFactory.decodeResource(getResources(), skyboxTextures[i]));
            }

            mCubeTexture = loadCubeMap(faces);
        }

        private int loadCubeMap(List<Bitmap> faces) {
            final int[] textureObjectIds = new int[1];
            int textureID;
            GLES30.glGenTextures(1, textureObjectIds, 0);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

            if (textureObjectIds[0] == 0) {
                LogUtil.e("Could not generate a new OpenGL texture object.");
                return -1;
            }

            textureID = textureObjectIds[0];

            int width, height;
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureID);

            for (int i = 0; i < faces.size(); i++) {
                Bitmap bmp = faces.get(i);
                width = bmp.getWidth();
                height = bmp.getHeight();
                int bytes = bmp.getByteCount();
                ByteBuffer buffer = ByteBuffer.allocate(bytes);
                bmp.copyPixelsToBuffer(buffer);
                buffer.position(0);

                GLES30.glTexImage2D(
                        GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0,
                        GLES30.GL_RGB, width, height, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, buffer
                );
            }

            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);

            return textureID;
        }
    }
}