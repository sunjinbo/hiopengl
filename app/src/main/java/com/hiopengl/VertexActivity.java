package com.hiopengl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VertexActivity extends AppCompatActivity {

    private enum GLType {
        VA, VBO, VAO, EBO
    }
    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mGLRenderer;
    private GLType mGLType = GLType.VA;
    private TextView mTypeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertex);
        mTypeTextView = findViewById(R.id.type);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLRenderer = new GLRenderer(this);
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

    public void onVAClick(View view) {
        mGLType = GLType.VA;
        mTypeTextView.setText("VA");
    }

    public void onVBOClick(View view) {
        mGLType = GLType.VBO;
        mTypeTextView.setText("VAO");
    }

    public void onVAOClick(View view) {
        mGLType = GLType.VAO;
        mTypeTextView.setText("VAO");
    }

    public void onEBOClick(View view) {
        mGLType = GLType.EBO;
        mTypeTextView.setText("EBO");
    }

    private class GLRenderer implements GLSurfaceView.Renderer {

        private static final int BYTES_PER_FLOAT = 4;
        private static final int BYTES_PER_INT = 4;

        protected Context mContext;

        //渲染程序
        protected int mProgram = -1;
        private FloatBuffer vertexBuffer;
        private FloatBuffer colorBuffer;
        private IntBuffer indexBuffer;

        float vertex[] ={
                0.5f,  0.5f, 0.0f, // top
                0.5f, -0.5f, 0.0f, // bottom left
                -0.5f, -0.5f, 0.0f  // bottom right
        };

        private float color[] = {
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f
        };

        private int index[] = {
                0, 1, 2
        };

        // VBO
        private int mVertexVboBufferId;
        private int mColorVboBufferId;
        // VAO
        private int mVaoBufferId;
        // EBO
        private int mEboBufferId;

        public GLRenderer(Context context) {
            mContext = context;
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuffer.asFloatBuffer();
            //把这门语法() 推送给GPU
            vertexBuffer.put(vertex);
            vertexBuffer.position(0);

            colorBuffer = ByteBuffer.allocateDirect(color.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            //传入指定的数据
            colorBuffer.put(color);
            colorBuffer.position(0);

            indexBuffer = ByteBuffer.allocateDirect(index.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
            //传入指定的数据
            indexBuffer.put(index);
            indexBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_glsl.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_glsl.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgram);

            initVertexVBO();
            initColorVBO();
            initEBO();
            initVAO();
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

            switch (mGLType) {
                case VA:
                    drawVA();
                    break;
                case VBO:
                    drawVBO();
                    break;
                case VAO:
                    drawVAO();
                    break;
                case EBO:
                    drawEBO();
                    break;
                default:
                    break;
            }
        }

        private void initVertexVBO() {
            // Allocate a buffer.
            final int buffers[] = new int[1];
            GLES30.glGenBuffers(buffers.length, buffers, 0);

            if (buffers[0] == 0) {
                throw new RuntimeException("Could not create a new index buffer object.");
            }

            mVertexVboBufferId = buffers[0];

            // Bind to the buffer.
            GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

            // Transfer data from native memory to the GPU buffer.
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES30.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            // We let the native buffer go out of scope, but it won't be released
            // until the next time the garbage collector is run.
        }

        private void initColorVBO() {
            // Allocate a buffer.
            final int buffers[] = new int[1];
            GLES30.glGenBuffers(buffers.length, buffers, 0);

            if (buffers[0] == 0) {
                throw new RuntimeException("Could not create a new index buffer object.");
            }

            mColorVboBufferId = buffers[0];

            // Bind to the buffer.
            GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

            // Transfer data from native memory to the GPU buffer.
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,colorBuffer.capacity() * BYTES_PER_FLOAT, colorBuffer, GLES30.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            // We let the native buffer go out of scope, but it won't be released
            // until the next time the garbage collector is run.
        }

        private void initEBO() {
            // Allocate a elements buffer object.
            final int buffers[] = new int[1];
            GLES30.glGenBuffers(buffers.length, buffers, 0);

            if (buffers[0] == 0) {
                throw new RuntimeException("Could not create a new index buffer object.");
            }

            mEboBufferId = buffers[0];

            // Bind to the buffer.
            GLES30.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

            // Transfer data from native memory to the GPU buffer.
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,indexBuffer.capacity() * BYTES_PER_INT, indexBuffer, GLES30.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

            // We let the native buffer go out of scope, but it won't be released
            // until the next time the garbage collector is run.
        }

        private void initVAO() {
            // Allocate a vertex array buffer.
            final int buffers[] = new int[1];
            GLES30.glGenVertexArrays(buffers.length, buffers, 0);

            if (buffers[0] == 0) {
                throw new RuntimeException("Could not create a new index buffer object.");
            }

            mVaoBufferId = buffers[0];

            // Bind to the buffer.
            GLES30.glBindVertexArray(buffers[0]);

            // 获取shader中顶点举柄的位置
            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            // 启用顶点句柄
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVboBufferId);
            GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            // 获取shader中颜色举柄的位置
            int aColorLocation = GLES30.glGetAttribLocation(mProgram,"aColor");
            // 启用颜色句柄
            GLES30.glEnableVertexAttribArray(aColorLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVboBufferId);
            GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES30.glBindVertexArray(0);
        }

        private void drawVA() {
            // 获取shader中顶点举柄的位置
            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            // 启用顶点句柄
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            // 将顶数据数组传递给GPU
            GLES30.glVertexAttribPointer(aPositionLocation,3, GLES30.GL_FLOAT,false,0, vertexBuffer);

            // 获取shader中颜色举柄的位置
            int aColorLocation = GLES30.glGetAttribLocation(mProgram,"aColor");
            // 将颜色数据数组传递给GPU
            GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
            // 启用颜色句柄
            GLES30.glEnableVertexAttribArray(aColorLocation);

            // 绘制图形
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

            // 关闭顶点和颜色数组的句柄
            GLES30.glDisableVertexAttribArray(aPositionLocation);
            GLES30.glDisableVertexAttribArray(aColorLocation);
        }

        private void drawVBO() {
            // 获取shader中顶点举柄的位置
            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            // 启用顶点句柄
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVboBufferId);
            GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            // 获取shader中颜色举柄的位置
            int aColorLocation = GLES30.glGetAttribLocation(mProgram,"aColor");
            // 启用颜色句柄
            GLES30.glEnableVertexAttribArray(aColorLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVboBufferId);
            GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            // 绘制图形
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

            // 关闭顶点和颜色数组的句柄
            GLES30.glDisableVertexAttribArray(aPositionLocation);
            GLES30.glDisableVertexAttribArray(aColorLocation);
        }

        private void drawVAO() {
            GLES30.glBindVertexArray(mVaoBufferId);

            // 绘制图形
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

            GLES30.glBindVertexArray(0);
        }

        private void drawEBO() {
            // 获取shader中顶点举柄的位置
            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"vPosition");
            // 启用顶点句柄
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVboBufferId);
            GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            // 获取shader中颜色举柄的位置
            int aColorLocation = GLES30.glGetAttribLocation(mProgram,"aColor");
            // 启用颜色句柄
            GLES30.glEnableVertexAttribArray(aColorLocation);
            // 直接使用GPU显存中的VBO
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVboBufferId);
            GLES30.glVertexAttribPointer(aColorLocation, 4, GLES30.GL_FLOAT,
                    false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mEboBufferId);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, 3, GLES30.GL_UNSIGNED_INT, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

            // 关闭顶点和颜色数组的句柄
            GLES30.glDisableVertexAttribArray(aPositionLocation);
            GLES30.glDisableVertexAttribArray(aColorLocation);
        }
    }

}
