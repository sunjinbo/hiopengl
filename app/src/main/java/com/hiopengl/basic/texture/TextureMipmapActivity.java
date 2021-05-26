package com.hiopengl.basic.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.LogUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureMipmapActivity extends ActionBarActivity {
    private GLSurfaceView mGLSurfaceView;
    private Texture3DRenderer mGLRenderer;
    private SeekBar mSeekBar;
    private int mDZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_mipmap);

        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDZ = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mDZ = mSeekBar.getProgress();
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new Texture3DRenderer(this);
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

    private class Texture3DRenderer implements GLSurfaceView.Renderer {
        private static final int BYTES_PER_FLOAT = 4;
        private static final int BYTES_PER_INT = 4;

        private Context mContext;

        // 渲染程序
        private int mProgram = -1;

        // 顶点和索引数据
        private FloatBuffer vertexBuffer;

        // VBO
        private int vboBufferId;

        // VAO
        private int vaoBufferId;

        // vertex数据(坐标+颜色+法向量)
        private float vertex[] ={ // X, Y, Z, S, T
                -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, // F
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, // B
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // C
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, // B
                -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, // F
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, // H

                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f, // E
                0.5f, -0.5f, 0.5f, 1.0f, 1.0f, // D
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // A
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // A
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, // G
                -0.5f, -0.5f, 0.5f, 0.0f, 1.0f, // E

                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // G
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, // H
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // F
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // F
                -0.5f, -0.5f, 0.5f, 1.0f, 1.0f, // E
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // G

                0.5f, 0.5f, -0.5f, 1.0f, 1.0f, // B
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // A
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // C
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // C
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, // A
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, // D

                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // F
                0.5f, -0.5f, -0.5f, 1.0f, 1.0f, // C
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, // D
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, // D
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, // E
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f, // F

                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, // H
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f, // A
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, // B
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, // G
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f, // A
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, // H
        };

        // 纹理
        private int textureId;

        //模型矩阵
        private final float[] mModelMatrix = new float[16];
        //相机矩阵
        private final float[] mViewMatrix = new float[16];
        //ViewModel矩阵
        private final float[] mViewModelMatrix = new float[16];
        //投影矩阵
        private final float[] mProjectMatrix = new float[16];
        //最终变换矩阵
        private final float[] mMVPMatrix = new float[16];

        public Texture3DRenderer(Context context) {
            mContext = context;
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuffer.asFloatBuffer();
            vertexBuffer.put(vertex);
            vertexBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_texture_3d.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_texture_3d.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgram);

            textureId = loadTexture(mContext, R.drawable.texture);

            // 初始化VBO
            int[] buffers = new int[1];
            GLES30.glGenBuffers(buffers.length, buffers, 0);
            if (buffers[0] == 0) {
                throw new RuntimeException();
            }

            vboBufferId = buffers[0];

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboBufferId);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES30.GL_STATIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            // 初始化VAO
            buffers[0] = 0;
            GLES30.glGenVertexArrays(buffers.length, buffers, 0);
            if (buffers[0] == 0) {
                throw new RuntimeException();
            }

            vaoBufferId = buffers[0];

            GLES30.glBindVertexArray(vaoBufferId);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboBufferId);
            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,5 * BYTES_PER_FLOAT, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboBufferId);
            int aTexCoordLocation = GLES30.glGetAttribLocation(mProgram,"aTexCoord");
            GLES30.glEnableVertexAttribArray(aTexCoordLocation);
            GLES30.glVertexAttribPointer(aTexCoordLocation,2, GLES30.GL_FLOAT,false,5 * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glBindVertexArray(0);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            // 设置透视投影
            Matrix.frustumM(mProjectMatrix,0, -ratio, ratio,-1f,1f,1f,333f);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mViewModelMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            Matrix.rotateM(mModelMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);

            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix,0,
                    0f,0f, mDZ,// 摄像机坐标
                    0f,0f,0f,// 目标物的中心坐标
                    0f,0.1f,0.0f);// 相机方向
            // 接着是摄像机顶部的方向了，如下图，很显然相机旋转，up的方向就会改变，这样就会会影响到绘制图像的角度。
            // 例如设置up方向为y轴正方向，upx = 0,upy = 1,upz = 0。这是相机正对着目标图像
            // 计算变换矩阵

            Matrix.multiplyMM(mViewModelMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix,0, mProjectMatrix,0, mViewModelMatrix,0);

            int uMatrixLocation = GLES30.glGetUniformLocation(mProgram,"vMatrix");
            GLES30.glUniformMatrix4fv(uMatrixLocation,1,false, mMVPMatrix,0);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
            GLES30.glUniform1i(uTextureLocation, 0);

            GLES30.glEnable(GL10.GL_CULL_FACE);
            GLES30.glCullFace(GLES30.GL_BACK);
            GLES30.glFrontFace(GLES30.GL_CCW);

            GLES30.glBindVertexArray(vaoBufferId);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36 * 3);
            GLES30.glBindVertexArray(0);
        }

        public int loadTexture(Context context, int resourceId) {
            final int[] textureObjectIds = new int[1];
            // 1. 创建纹理对象
            GLES30.glGenTextures(1, textureObjectIds, 0);

            if (textureObjectIds[0] == 0) {
                LogUtil.e("Could not generate a new OpenGL texture object.");
                return -1;
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            final Bitmap bitmap = BitmapFactory.decodeResource(
                    context.getResources(), resourceId, options);

            if (bitmap == null) {
                LogUtil.e("Resource ID " + resourceId + " could not be decoded.");
                // 加载Bitmap资源失败，删除纹理Id
                GLES30.glDeleteTextures(1, textureObjectIds, 0);
                return -1;
            }
            // 2. 将纹理绑定到OpenGL对象上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0]);

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_MIRRORED_REPEAT);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_MIRRORED_REPEAT);

            // 3. 设置纹理过滤参数:解决纹理缩放过程中的锯齿问题。若不设置，则会导致纹理为黑色
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            // 4. 通过OpenGL对象读取Bitmap数据，并且绑定到纹理对象上，之后就可以回收Bitmap对象
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // Note: Following code may cause an error to be reported in the
            // ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
            // Failed to generate texture mipmap levels (error=3)
            // No OpenGL error will be encountered (glGetError() will return
            // 0). If this happens, just squash the source image to be
            // square. It will look the same because of texture coordinates,
            // and mipmap generation will work.
            // 5. 生成Mip位图
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

            // 6. 回收Bitmap对象
            bitmap.recycle();

            // 7. 将纹理从OpenGL对象上解绑
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            // 所以整个流程中，OpenGL对象类似一个容器或者中间者的方式，将Bitmap数据转移到OpenGL纹理上
            return textureObjectIds[0];
        }
    }
}