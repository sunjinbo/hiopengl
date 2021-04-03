package com.hiopengl.practices;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ParticleView extends GLSurfaceView implements GLSurfaceView.Renderer, Runnable {
    private final static String A_POSITION = "a_Position";
    private static final int LENGTH_OF_PARTICLE = 4; // x, y, z, w
    private static final int MAX_PARTICLES_NUM = 300;
    private static final long FRAME_INTERVAL_TIME = 1000L / 16;

    private int mProgramId;

    // 临时测试数据
    private float[] mData = new float[MAX_PARTICLES_NUM * LENGTH_OF_PARTICLE];
    private FloatBuffer mFloatBuffer;

    private List<Particle> mParticleList;
    private Random mRandom = new Random();
    private Path mPath;
    private PathMeasure mPathMeasure;
    private int mWidth, mHeight;

    public ParticleView(Context context) {
        super(context);
        initView(context);
    }

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mParticleList = new ArrayList<>();
        mPath = new Path();
        mPathMeasure = new PathMeasure();

        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        final String vertexShader = ShaderUtil.loadAssets(getContext(), "vertex_particle.glsl");
        final int vertexShaderId = ShaderUtil.compileVertexShader(vertexShader);
        final String fragmentShader = ShaderUtil.loadAssets(getContext(), "fragment_particle.glsl");
        final int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShader);
        mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mData.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mFloatBuffer = byteBuffer.asFloatBuffer();

        new Thread(this).start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        GLES20.glViewport(0, 0, w, h);

        mWidth = w;
        mHeight = h;

        float centerX = w / 2f;
        float centerY = h / 2f;
        float radius = Math.min(w / 4f, h / 4f);

        mPath.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        mPathMeasure.setPath(mPath, true);

        float[] pos = new float[2];
        float[] tan = new float[2];

        mParticleList.clear();
        for (int i = 0; i < MAX_PARTICLES_NUM; ++i) {
            mPathMeasure.getPosTan((float)i / (float)MAX_PARTICLES_NUM * mPathMeasure.getLength(), pos, tan);
            float x = pos[0] + mRandom.nextInt(6) - 3f; // X值随机偏移
            float y =  pos[1] + mRandom.nextInt(6) - 3f; // Y值随机偏移
            float speed = mRandom.nextInt(10) + 5;
            float angle = (float) getAngle(x, y, centerX, centerY, radius);
            Particle p = new Particle(x, y, 3, angle, speed, Color.RED);

            x = x / (float) mWidth * 2 - 1.0f;
            y = y / (float) mHeight * 2 - 1.0f;

            mData[i * LENGTH_OF_PARTICLE] = x;
            mData[i * LENGTH_OF_PARTICLE + 1] = y;
            mData[i * LENGTH_OF_PARTICLE + 2] = 0.0f;
            mData[i * LENGTH_OF_PARTICLE + 3] = p.getLife() / 255f;

            mParticleList.add(p);
        }

        mFloatBuffer.put(mData, 0, mData.length);
        mFloatBuffer.position(0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);

        int aPositionLocation = GLES20.glGetAttribLocation(mProgramId, A_POSITION);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation,4, GLES20.GL_FLOAT, false, 0, mFloatBuffer);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, MAX_PARTICLES_NUM);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void run() {
        while (true) {
            long startTime = SystemClock.elapsedRealtime();

            float[] arr = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
            for (int i = 0; i < arr.length; ++i) {
                arr[i] = arr[i] / 100f * 2 - 1.0f;
            }

            for (int i = 0; i < mParticleList.size(); ++i) {
                mParticleList.get(i).step();
                float x = mParticleList.get(i).getX() / (float) mWidth * 2 - 1.0f;
                float y = mParticleList.get(i).getY() / (float) mHeight * 2 - 1.0f;
                mData[i * LENGTH_OF_PARTICLE] = x;
                mData[i * LENGTH_OF_PARTICLE + 1] = y;
                mData[i * LENGTH_OF_PARTICLE + 2] = 0.0f;
                mData[i * LENGTH_OF_PARTICLE + 3] = mParticleList.get(i).getLife() / 255f;
            }
            mFloatBuffer.put(mData);
            mFloatBuffer.position(0);

            long calcTime = SystemClock.elapsedRealtime() - startTime;
            long delayTime = FRAME_INTERVAL_TIME - (long)(calcTime / 1000F);
            if (delayTime > 0L) {
                SystemClock.sleep(delayTime);
            }
        }
    }

    private double getAngle(float x, float y, float centerX, float centerY, float radius) {
        double angle = 0F;
        if (x - centerX > 0F && y - centerY == 0F) { // 在x轴的正向轴上
            angle = 0F;
        } else if (x - centerX > 0F && y - centerY > 0F) { // 在第一象限
            angle = Math.acos(((x - centerX) / radius)); // 反余弦函数可以得到弧度值
        } else if (x - centerX == 0f && y - centerY < 0F) { // 在y轴的正向轴上
            angle = Math.PI / 2f;
        } else if (x - centerX < 0F && y - centerY > 0F) { // 在第二象限
            angle = Math.PI - Math.acos(((centerX - x) / radius)); // 反余弦函数可以得到弧度值
        } else if (x - centerX < 0F && y - centerY == 0F) { // 在x轴的负向轴上
            angle = Math.PI;
        } else if (x - centerX < 0F && y - centerY < 0F) { // 在第三象限
            angle = Math.PI + Math.acos(((centerX - x) / radius)); // 反余弦函数可以得到弧度值
        } else if (x - centerX == 0f && y - centerY > 0F) { // 在y轴的负向轴上
            angle = Math.PI * 1.5F;
        } else if (x - centerX > 0F && y - centerY < 0F) { // 在第四象限
            angle = Math.PI * 2F - Math.acos(((x - centerX) / radius)); // 反余弦函数可以得到弧度值
        }

        return angle;
    }
}
