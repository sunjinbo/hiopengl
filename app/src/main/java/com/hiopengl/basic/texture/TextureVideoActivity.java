package com.hiopengl.basic.texture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureVideoActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLVideoRenderer mGLVideoRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_video);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLVideoRenderer = new GLVideoRenderer(this);
        mGLSurfaceView.setRenderer(mGLVideoRenderer);
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
    protected void onDestroy() {
        super.onDestroy();
        mGLVideoRenderer.stopPlayer();
    }

    public class GLVideoRenderer implements GLSurfaceView.Renderer,
            SurfaceTexture.OnFrameAvailableListener,
            MediaPlayer.OnVideoSizeChangedListener  {
        private static final String TAG = "GLRenderer";

        private Context mContext;
        private int aPositionLocation;
        private int mProgramId;

        private FloatBuffer vertexBuffer;
        private final float[] vertexData = {
                1f,-1f,0f,
                -1f,-1f,0f,
                1f,1f,0f,
                -1f,1f,0f
        };

        private final float[] projectionMatrix=new float[16];
        private int uMatrixLocation;

        private final float[] textureVertexData = {
                1f,0f,
                0f,0f,
                1f,1f,
                0f,1f
        };
        private FloatBuffer textureVertexBuffer;
        private int uTextureSamplerLocation;
        private int aTextureCoordLocation;
        private int textureId;

        private SurfaceTexture surfaceTexture;
        private MediaPlayer mMediaPlayer;
        private float[] mSTMatrix = new float[16];
        private int uSTMMatrixHandle;

        private boolean mUpdateSurface;
        private boolean mPlayerPrepared;
        private int mScreenWidth, mScreenHeight;

        public GLVideoRenderer(Context context) {
            mContext = context;
            mPlayerPrepared = false;
            synchronized(this) {
                mUpdateSurface = false;
            }
            vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertexData);
            vertexBuffer.position(0);

            textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(textureVertexData);
            textureVertexBuffer.position(0);


            try {
                mMediaPlayer = new MediaPlayer();
                Uri uri = Uri.parse("android.resource://com.hiopengl/" + R.raw.sintel_trailer_480p);
                mMediaPlayer.setDataSource(context, uri);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setOnVideoSizeChangedListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_video.glsl");
            int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_video.glsl");
            int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
            //连接程序
            mProgramId = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES30.glUseProgram(mProgramId);

            aPositionLocation= GLES20.glGetAttribLocation(mProgramId,"aPosition");

            uMatrixLocation=GLES20.glGetUniformLocation(mProgramId,"uMatrix");
            uSTMMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uSTMatrix");
            uTextureSamplerLocation=GLES20.glGetUniformLocation(mProgramId,"sTexture");
            aTextureCoordLocation=GLES20.glGetAttribLocation(mProgramId,"aTexCoord");

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            textureId = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GlUtil.checkGlError("glBindTexture mTextureID");
            /*GLES11Ext.GL_TEXTURE_EXTERNAL_OES的用处？
              之前提到视频解码的输出格式是YUV的（YUV420p，应该是），那么这个扩展纹理的作用就是实现YUV格式到RGB的自动转化，
              我们就不需要再为此写YUV转RGB的代码了*/
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(this);//监听是否有新的一帧数据到来

            Surface surface = new Surface(surfaceTexture);
            mMediaPlayer.setSurface(surface);
            surface.release();

            if (!mPlayerPrepared){
                try {
                    mMediaPlayer.prepare();
                    mPlayerPrepared = true;
                } catch (Exception t) {
                    Log.e(TAG, "media player prepare failed");
                }
                mMediaPlayer.start();
                mPlayerPrepared = true;
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged: "+width+" "+height);
            mScreenWidth = width;
            mScreenHeight=height;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClearColor(0.2F, 0.2F, 0.2F, 1.0F);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            synchronized (this){
                if (mUpdateSurface){
                    surfaceTexture.updateTexImage();//获取新数据
                    surfaceTexture.getTransformMatrix(mSTMatrix);//让新的纹理和纹理坐标系能够正确的对应,mSTMatrix的定义是和projectionMatrix完全一样的。
                    mUpdateSurface = false;
                }
            }
            GLES20.glUseProgram(mProgramId);
            GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0);
            GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);

            vertexBuffer.position(0);
            GLES20.glEnableVertexAttribArray(aPositionLocation);
            GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false,
                    12, vertexBuffer);

            textureVertexBuffer.position(0);
            GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
            GLES20.glVertexAttribPointer(aTextureCoordLocation,2,GLES20.GL_FLOAT,false,8,textureVertexBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

            GLES20.glUniform1i(uTextureSamplerLocation,0);
            GLES20.glViewport(0,0, mScreenWidth, mScreenHeight);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }

        @Override
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            mUpdateSurface = true;
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged: "+width+" "+height);
            updateProjection(width,height);
        }

        private void updateProjection(int videoWidth, int videoHeight){
            float screenRatio = (float)mScreenWidth / (float)mScreenHeight;
            float videoRatio = (float)videoWidth/videoHeight;
            if (videoRatio>screenRatio){
                Matrix.orthoM(projectionMatrix,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);
            }else Matrix.orthoM(projectionMatrix,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
        }

        public void stopPlayer() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }
    }
}