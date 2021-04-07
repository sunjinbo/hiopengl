package com.hiopengl.android.recorder;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RecorderActivity extends ActionBarActivity
        implements GLSurfaceView.Renderer, Runnable {

    protected static final float BALL_RADIUS = 100f;

    protected GLSurfaceView mGLSurfaceView;
    protected Playground mPlayground;

    protected int mProgram = -1;
    protected int mTextureId = -1;
    protected FloatBuffer mVertexBuffer;
    protected float mVertexArray[] = {
            -0.5f, -0.4f, 0.0f, BALL_RADIUS * 2 };

    protected boolean mIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(this);

        ShaderUtil.setEGLContextClientVersion(3);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertexArray.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(mVertexArray);
        mVertexBuffer.position(0);
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
    protected void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);

        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(this, "vertex_recorder.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(this, "fragment_recorder.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        mPlayground = new Playground(width, height);
        // 启动小球运动程序
        new Thread(this).start();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        if (mTextureId < 0) {
            // 生成ball纹理
            mTextureId = GlUtil.loadTexture(this, R.drawable.texture);
        }

        // 设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        // 将纹理单元传递片段着色器的u_TextureUnit
        int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"uTexture");
        GLES30.glUniform1i(uTextureLocation, 0);

        GLES30.glVertexAttribPointer(0, 4, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
    }

    @Override
    public void run() {
        mIsRunning = true;
        while (mIsRunning) {
            SystemClock.sleep(33);
            mPlayground.ball.step();
            mVertexArray[0] = mPlayground.ball.getX();
            mVertexArray[1] = mPlayground.ball.getY();

            mVertexBuffer.put(mVertexArray);
            mVertexBuffer.position(0);
        }
    }

    public class Ball {
        public Playground playground;
        public float x;
        public float y;
        public float dx;
        public float dy;
        public float radius;

        public Ball(Playground playground, float x, float y, float dx, float dy, float radius) {
            this.playground = playground;
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.radius = radius;
        }

        public void step() {
            this.x += dx;
            this.y += dy;

            // 开始边缘碰撞检测
            if (this.x - radius <= 0) {
                this.x = radius;
                this.dx *= -1;
            } else if (this.x + radius >= playground.width) {
                this.x = playground.width - radius;
                this.dx *= -1;
            }

            if (this.y - radius <= 0) {
                this.y = radius;
                this.dy *= -1;
            } else if (this.y + radius >= playground.height) {
                this.y = playground.height - radius;
                this.dy *= -1;
            }
        }

        public float getX() {
            return (x / playground.width) * 2f - 1f;
        }

        public float getY() {
            return (y / playground.height) * 2f - 1f;
        }
    }

    public class Playground {
        public float width;
        public float height;
        public Ball ball;

        private Random random = new Random();

        public Playground(float width, float height) {
            this.width = width;
            this.height = height;
            this.ball = generate(width, height, BALL_RADIUS);
        }

        private Ball generate(float width, float height, float radius) {
            Ball b = new Ball(this,
                    (random.nextInt((int) (width - radius * 2)) + radius),
                    (random.nextInt((int) (height - radius * 2)) + radius),
                    9f,
                    -11f,
                    radius);
            return b;
        }
    }
}