package com.hiopengl.advanced;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.android.recorder.TextureDrawer;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.LogUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FrameBufferActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    private GLSurfaceView mGLSurfaceView;

    private int mOffscreenTexture;
    private int mFramebuffer;
    private int mDepthBuffer;

    private TextureDrawer mTextureDrawer;
    private int mProgram = -1;
    private int mTextureId = -1;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textBuffer;

    //3个定点，等腰直角
    float triangleCoords[] = {
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f,  0.5f, 0.0f, // top
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f,  0.5f, 0.0f, // top
            -0.5f, 0.5f, 0.0f  // top left
    };

    float textCoords[] ={
            0.0f,  0.0f, // bottom left
            1.0f, 1.0f, // top
            1.0f, 0.0f,  // bottom right
            1.0f,  1.0f, // top
            0.0f, 1.0f // top left
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_buffer);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(this);
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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

        initProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        mTextureDrawer = new TextureDrawer(this,
                "vertext_grey_texture.glsl", "fragment_grey_texture.glsl",
                width, height);
        prepareFramebuffer(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);
        drawProgram();
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        if (mTextureDrawer != null) {
            mTextureDrawer.drawTexture(mOffscreenTexture);
        }
    }

    private void initProgram() {
        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(this, "vertex_texture_2d.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(this, "fragment_texture_2d.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        mTextureId = loadTexture(this, R.drawable.cat);
    }

    private void drawProgram() {
        GLES30.glUseProgram(mProgram);

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

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 5);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }

    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("prepareFramebuffer start");

        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES30.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture " + mOffscreenTexture);

        // Create texture storage.
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_NEAREST);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES30.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        // Create a depth buffer and bind it.
        GLES30.glGenRenderbuffers(1, values, 0);
        GlUtil.checkGlError("glGenRenderbuffers");
        mDepthBuffer = values[0];    // expected > 0
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, mDepthBuffer);
        GlUtil.checkGlError("glBindRenderbuffer " + mDepthBuffer);

        // Allocate storage for the depth buffer.
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16,
                width, height);
        GlUtil.checkGlError("glRenderbufferStorage");

        // Attach the depth buffer and the texture (color buffer) to the framebuffer object.
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
                GLES30.GL_RENDERBUFFER, mDepthBuffer);
        GlUtil.checkGlError("glFramebufferRenderbuffer");
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, mOffscreenTexture, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        // Switch back to the default framebuffer.
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GlUtil.checkGlError("prepareFramebuffer done");
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