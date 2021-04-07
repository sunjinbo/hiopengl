package com.hiopengl.android.recorder;

import android.opengl.EGLExt;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGL14.EGL_BLUE_SIZE;
import static android.opengl.EGL14.EGL_DEPTH_SIZE;
import static android.opengl.EGL14.EGL_GREEN_SIZE;
import static android.opengl.EGL14.EGL_NONE;
import static android.opengl.EGL14.EGL_RED_SIZE;
import static android.opengl.EGL14.EGL_RENDERABLE_TYPE;

public class RecorderActivity extends ActionBarActivity
        implements SurfaceHolder.Callback, Runnable {

    protected static final float BALL_RADIUS = 100f;

    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mSurfaceHolder;
    protected Playground mPlayground;
    protected int mWidth;
    protected int mHeight;

    protected int mProgram = -1;
    protected int mTextureId = -1;
    protected FloatBuffer mVertexBuffer;
    protected float mVertexArray[] = {
            -0.5f, -0.4f, 0.0f, BALL_RADIUS * 2 };

    protected boolean mIsRunning = false;

    protected Button mRecordButton;
    protected boolean mIsRecording = false;

    protected long mLastTickTimeMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        mRecordButton = findViewById(R.id.recorder);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    mIsRecording = false;
                    mRecordButton.setText("START RECORD");
                } else {
                    mIsRecording = true;
                    mRecordButton.setText("STOP RECORD");
                }
            }
        });

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);

        ShaderUtil.setEGLContextClientVersion(3);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertexArray.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(mVertexArray);
        mVertexBuffer.position(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
    }

    @Override
    public void run() {
        //创建一个EGL实例
        EGL10 egl = (EGL10) EGLContext.getEGL();
        //
        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //初始化EGLDisplay
        int[] version = new int[2];
        egl.eglInitialize(dpy, version);

        int[] configSpec = {
                EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL_RED_SIZE, 5,
                EGL_GREEN_SIZE, 6,
                EGL_BLUE_SIZE, 5,
                EGL_DEPTH_SIZE, 1,
                EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        //选择config创建opengl运行环境
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];
        int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(dpy, config,
                EGL10.EGL_NO_CONTEXT, attrib_list);
        //创建新的surface
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceHolder, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        initGL(gl);

        mIsRunning = true;
        while (mIsRunning) {
            synchronized (mSurfaceHolder) {
                tick();

                drawFrame(gl);

                //显示绘制结果到屏幕上
                egl.eglSwapBuffers(dpy, surface);
            }
        }

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surface);
        egl.eglDestroyContext(dpy, context);
        egl.eglTerminate(dpy);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        mWidth = width;
        mHeight = height;
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }

    private void initGL(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        mPlayground = new Playground(mWidth, mHeight);

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

        // 生成ball纹理
        mTextureId = GlUtil.loadTexture(this, R.drawable.ball);
    }

    private void drawFrame(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

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

    private void tick() {
        mLastTickTimeMillis = SystemClock.elapsedRealtime();

        mPlayground.ball.step();
        mVertexArray[0] = mPlayground.ball.getX();
        mVertexArray[1] = mPlayground.ball.getY();

        mVertexBuffer.put(mVertexArray);
        mVertexBuffer.position(0);
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