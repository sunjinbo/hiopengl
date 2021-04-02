package com.hiopengl.android.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.opengl.EGLExt;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.GlUtil;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

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

public class CameraFilterActivity extends ActionBarActivity
        implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener, Runnable {

    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureBuilder;
    private CameraCaptureSession mCaptureSession;
    private String mCameraId;

    private boolean mRunning = false;
    private SurfaceTexture mSurfaceTexture;
    private int mTextureId = -1;

    private SurfaceTexture mCameraTexture;
    private boolean mUpdateTexture = false;

    private int mWidth = 0;
    private int mHeight = 0;

    //渲染程序
    private int mProgram = -1;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textBuffer;

    //3个定点，等腰直角
    float triangleCoords[] ={
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
        setContentView(R.layout.activity_camera_filter);
        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);
        int isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (isGranted == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults != null && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    openCamera();
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        new Thread(this).start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mRunning = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mUpdateTexture = true;
        }
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
        EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, mSurfaceTexture, null);
        //将opengles环境设置为当前
        egl.eglMakeCurrent(dpy, surface, surface, context);
        //获取当前opengles画布
        GL10 gl = (GL10)context.getGL();

        initGL(); // 初始化GL

        mRunning = true;
        while (mRunning) {
            SystemClock.sleep(333);
            synchronized (mSurfaceTexture) {
                updateTexture();
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

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String[] cameraIds = mCameraManager.getCameraIdList();
                for (String cameraId : cameraIds) {
                    // 查询摄像头属性
                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                    if (characteristics.get(CameraCharacteristics.LENS_FACING)
                            == CameraCharacteristics.LENS_FACING_BACK) { // 选择后置摄像头
                        mCameraId = cameraId;
                        break;
                    }
                }

                mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        mCameraDevice = camera;
                        startPreview();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        if (mCameraDevice != null) {
                            mCameraDevice.close();
                            mCameraDevice = null;
                        }
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        if (mCameraDevice != null) {
                            mCameraDevice.close();
                            mCameraDevice = null;
                        }
                    }
                }, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCameraTexture = new SurfaceTexture(mTextureId);
            mCameraTexture.setOnFrameAvailableListener(this);
            Surface surface = new Surface(mCameraTexture);
            mCaptureBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        mCaptureSession = cameraCaptureSession;
                        // 设置为自动对焦
                        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), null, null);// 设置成预览
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    stopPreview();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void initGL() {
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

        ShaderUtil.setEGLContextClientVersion(3);

        //编译顶点着色程序
        String vertexShaderStr = ShaderUtil.loadAssets(this, "vertex_camera_filter.glsl");
        int vertexShaderId = ShaderUtil.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        String fragmentShaderStr = ShaderUtil.loadAssets(this, "fragment_camera_filter.glsl");
        int fragmentShaderId = ShaderUtil.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtil.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES30.glUseProgram(mProgram);

        mTextureId = createTextureObject();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                openCamera();
            }
        });
    }

    private void drawFrame(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        GLES30.glVertexAttribPointer(0,3, GLES30.GL_FLOAT,false,0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, textBuffer);
        GLES30.glEnableVertexAttribArray(1);

        // 设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        // 将纹理单元传递片段着色器的u_TextureUnit
        int uTextureLocation = GLES30.glGetUniformLocation(mProgram,"sTexture");
        GLES30.glUniform1i(uTextureLocation, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 5);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }

    public int createTextureObject() {
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        GlUtil.checkGl3Error("glGenTextures");

        int texId = textures[0];
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);
        GlUtil.checkGl3Error("glBindTexture " + texId);

        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGl3Error("glTexParameter");

        return texId;
    }

    private void updateTexture() {
        synchronized (this) {
            if (mUpdateTexture) {
                mCameraTexture.updateTexImage();
                mUpdateTexture = false;
            }
        }
    }
}