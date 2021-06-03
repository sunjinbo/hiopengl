package com.hiopengl.practices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        private int mSkyboxProgram;
        private int mSkyboxTexture = 0;

        private int mCubeProgram;

        private int skyboxTextures[] = {
                R.drawable.right,
                R.drawable.left,
                R.drawable.top,
                R.drawable.bottom,
                R.drawable.back,
                R.drawable.front
        };

        private float cubeVertices[] = {
                // Positions          // Texture Coords
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
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

        private FloatBuffer mSkyboxBuffer;
        private FloatBuffer mCubeBuffer;

        private float[] mModelMatrix = new float[16]; // 相机矩阵
        private float[] mViewMatrix = new float[16]; // 相机矩阵
        private float[] mProjectionMatrix = new float[16]; // 投影矩阵
        private float[] mCameraPos = new float[9];

        public SkyboxRenderer(Context context) {
            mContext = context;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            initCubeProgram();
            initCubeData();

            initSkyboxProgram();
            initSkyboxData();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.frustumM(mProjectionMatrix,0, -ratio, ratio,-1f,1f,0.1f, 100.0f);
            Matrix.setIdentityM(mViewMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            drawCube();
//            drawSkybox();
        }

        private void initSkyboxProgram() {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_skybox.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            GlUtil.checkGl3Error("Check Vertex Shader!");
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_skybox.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            GlUtil.checkGl3Error("Check Fragment Shader!");
            //连接程序
            mSkyboxProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            GlUtil.checkGl3Error("Check GL Program!");
        }

        private void initSkyboxData() {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(skyboxVertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            mSkyboxBuffer = byteBuffer.asFloatBuffer();
            mSkyboxBuffer.put(skyboxVertices);
            mSkyboxBuffer.position(0);

            List<Bitmap> faces = new ArrayList<>();
            for (int i = 0; i < skyboxTextures.length; ++i) {
                faces.add(BitmapFactory.decodeResource(getResources(), skyboxTextures[i]));
            }

            mSkyboxTexture = loadCubemapsTexture(faces);
        }

        private void drawSkybox() {
            GLES30.glUseProgram(mSkyboxProgram);

            GLES30.glDepthMask(false);

            Matrix.rotateM(mViewMatrix, 0, 0.1f, 0f, 1f, 0f);
            int viewLocation = GLES30.glGetUniformLocation(mSkyboxProgram,"view");
            GLES30.glUniformMatrix4fv(viewLocation,1,false, mViewMatrix,0);

            int projectionLocation = GLES30.glGetUniformLocation(mSkyboxProgram,"projection");
            GLES30.glUniformMatrix4fv(projectionLocation,1,false, mProjectionMatrix,0);

            int aPositionLocation = GLES30.glGetAttribLocation(mSkyboxProgram,"position");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,0, mSkyboxBuffer);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, mSkyboxTexture);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mSkyboxProgram,"skybox");
            GLES30.glUniform1i(uTextureLocation, 0);

            GLES20.glDrawArrays(GL10.GL_TRIANGLES, 0, 36);

            GLES30.glDisableVertexAttribArray(aPositionLocation);

            GLES30.glDepthMask(true);
        }

        private int loadCubemapsTexture(List<Bitmap> faces) {
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
                        GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer
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

        private void initCubeProgram() {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_cube.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            GlUtil.checkGl3Error("Check Vertex Shader!");
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_cube.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            GlUtil.checkGl3Error("Check Fragment Shader!");
            //连接程序
            mCubeProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            GlUtil.checkGl3Error("Check GL Program!");
        }

        private void initCubeData() {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubeVertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            mCubeBuffer = byteBuffer.asFloatBuffer();
            mCubeBuffer.put(cubeVertices);
            mCubeBuffer.position(0);
        }

        private void drawCube() {
            GLES30.glUseProgram(mCubeProgram);

            GLES30.glDepthMask(false);

            int modelLocation = GLES30.glGetUniformLocation(mCubeProgram,"model");
            GLES30.glUniformMatrix4fv(modelLocation,1,false, mModelMatrix,0);
            int viewLocation = GLES30.glGetUniformLocation(mCubeProgram,"view");
            GLES30.glUniformMatrix4fv(viewLocation,1,false, mViewMatrix,0);
            int projectionLocation = GLES30.glGetUniformLocation(mCubeProgram,"projection");
            GLES30.glUniformMatrix4fv(projectionLocation,1,false, mProjectionMatrix,0);
            int cameraPosLocation = GLES30.glGetUniformLocation(mCubeProgram,"cameraPos");
            GLES30.glUniformMatrix3fv(cameraPosLocation,1,false, mCameraPos,0);

            int aPositionLocation = GLES30.glGetAttribLocation(mCubeProgram,"position");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,0, mCubeBuffer);

            int aNormalLocation = GLES30.glGetAttribLocation(mCubeProgram,"normal");
            GLES30.glEnableVertexAttribArray(aNormalLocation);
            GLES30.glVertexAttribPointer(aNormalLocation,3, GLES30.GL_FLOAT,false,3 * 4, mCubeBuffer);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, mSkyboxTexture);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mSkyboxProgram,"skybox");
            GLES30.glUniform1i(uTextureLocation, 0);

            GLES20.glDrawArrays(GL10.GL_TRIANGLES, 0, 36);

            GLES30.glDisableVertexAttribArray(aPositionLocation);

            GLES30.glDepthMask(true);
        }
    }
}
