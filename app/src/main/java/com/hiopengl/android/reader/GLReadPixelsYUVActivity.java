package com.hiopengl.android.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.widget.Button;

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

public class GLReadPixelsYUVActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private Button mRGB2YUV;
    private volatile boolean mRGB2YUVFlag = false;
    private ByteBuffer mPixelBuf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glreadpixels_yuv);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(new GLYUVRenderer());
        mRGB2YUV = findViewById(R.id.rgb_to_yuv);
        mRGB2YUV.setOnClickListener(v -> {
            mRGB2YUVFlag = true;
        });
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

    private class GLYUVRenderer implements GLSurfaceView.Renderer {

        private int mWidth;
        private int mHeight;

        private int mFBOId;

        //3个定点，等腰直角
        private float triangleCoords[] = {
                -1f, -1f, 0.0f,  // bottom left
                1f,  1f, 0.0f, // top
                1f, -1f, 0.0f, // bottom right
                1f,  1f, 0.0f, // top
                -1f, 1f, 0.0f  // top left
        };

        private float textCoords[] ={
                0.0f,  0.0f, // bottom left
                1.0f, 1.0f, // top
                1.0f, 0.0f,  // bottom right
                1.0f,  1.0f, // top
                0.0f, 1.0f // top left
        };

        private int mProgram = -1;
        private FloatBuffer vertexBuffer;
        private FloatBuffer textBuffer;

        private int mTextureId;
        private int mFramebufferTextureId;

        private float mTextureOffset;

        public GLYUVRenderer() {
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
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(GLReadPixelsYUVActivity.this, "vertex_glreadpixels_yuv.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(GLReadPixelsYUVActivity.this, "fragment_glreadpixels_yuv.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgram);

            mTextureId = loadTexture(GLReadPixelsYUVActivity.this, R.drawable.cat);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES30.glViewport(0, 0, width, height);
            mWidth = width;
            mHeight = height;
            mPixelBuf = ByteBuffer.allocateDirect(width / 2 * height * 4);
            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
            mTextureOffset = (float) (1.f / (float) width);
            initFrameBufferObj();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            GLES30.glUseProgram(mProgram);

            if (mRGB2YUVFlag) {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOId);
                // 渲染成 yuyv 宽度像素减半,glviewport 宽度减半
                GLES30.glViewport(0, 0, mWidth / 2, mHeight);
            } else {
                GLES30.glViewport(0, 0, mWidth, mHeight);
            }

            GLES30.glVertexAttribPointer(0,3, GLES30.GL_FLOAT,false,0, vertexBuffer);
            GLES30.glEnableVertexAttribArray(0);

            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textBuffer);
            GLES30.glEnableVertexAttribArray(1);

            // 设置当前活动的纹理单元为纹理单元0
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            // 将纹理ID绑定到当前活动的纹理单元上
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
            // 将纹理单元传递片段着色器的u_TextureUnit
            int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
            GLES30.glUniform1i(uTextureLocation, 0);

            int uRGB2YUVLocation = GLES30.glGetUniformLocation(mProgram,"uRGB2YUV");
            GLES30.glUniform1ui(uRGB2YUVLocation, mRGB2YUVFlag ? 1 : 0);

            int uOffsetLocation = GLES30.glGetUniformLocation(mProgram,"uOffset");
            GLES30.glUniform1f(uOffsetLocation, mTextureOffset);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 5);

            //禁止顶点数组的句柄
            GLES30.glDisableVertexAttribArray(0);
            GLES30.glDisableVertexAttribArray(1);

            if (mRGB2YUVFlag) {
                mRGB2YUVFlag = false;
                GLES30.glReadPixels(0, 0, mWidth / 2, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mPixelBuf);
                mPixelBuf.rewind();
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            }
        }

        private void initFrameBufferObj() {
            // 创建并初始化 FBO 纹理
            final int[] textureObjectIds = new int[1];
            GLES30.glGenTextures(1, textureObjectIds, 0);
            mFramebufferTextureId = textureObjectIds[0];
            GlUtil.checkGlError("glGenTextures");
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFramebufferTextureId);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE);

            // 创建并初始化 FBO
            int[] buffers = new int[1];
            GLES30.glGenFramebuffers(1, buffers, 0);
            mFBOId = buffers[0];

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOId);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFramebufferTextureId);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mFramebufferTextureId, 0);
            //FBO 纹理是用来容纳 YUYV 数据，其宽度应该设置为原图的一半
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth / 2, mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE) {
                return;
            }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE);
        }

        private int loadTexture(Context context, int resourceId) {
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
            if (bitmap != null) {
                android.graphics.Matrix m = new android.graphics.Matrix();
                m.setScale(1, -1); // 垂直翻转
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                // 生成的翻转后的bitmap
                final Bitmap flipBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
                if (flipBitmap == null) {
                    LogUtil.e("Resource ID " + resourceId + " could not be decoded.");
                    // 加载Bitmap资源失败，删除纹理Id
                    GLES30.glDeleteTextures(1, textureObjectIds, 0);
                    return -1;
                }

                // 2. 将纹理绑定到OpenGL对象上
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0]);

                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);

                // 3. 设置纹理过滤参数:解决纹理缩放过程中的锯齿问题。若不设置，则会导致纹理为黑色
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                // 4. 通过OpenGL对象读取Bitmap数据，并且绑定到纹理对象上，之后就可以回收Bitmap对象
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, flipBitmap, 0);

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
                flipBitmap.recycle();

                // 7. 将纹理从OpenGL对象上解绑
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

                // 所以整个流程中，OpenGL对象类似一个容器或者中间者的方式，将Bitmap数据转移到OpenGL纹理上
                return textureObjectIds[0];
            } else {
                GLES30.glDeleteTextures(1, textureObjectIds, 0);
                return -1;
            }
        }
    }
}