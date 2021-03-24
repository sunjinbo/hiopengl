package com.hiopengl;

import android.graphics.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    private static final int CONFIG_CHOOSER_RED_SIZE = 8;
    private static final int CONFIG_CHOOSER_GREEN_SIZE = 8;
    private static final int CONFIG_CHOOSER_BLUE_SIZE = 8;
    private static final int CONFIG_CHOOSER_ALPHA_SIZE = 8;
    private static final int CONFIG_CHOOSER_DEPTH_SIZE = 16;
    private static final int CONFIG_CHOOSER_STENCIL_SIZE = 0;

    private GLSurfaceView mGLSurfaceView;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setEGLConfigChooser(CONFIG_CHOOSER_RED_SIZE,
                CONFIG_CHOOSER_GREEN_SIZE,
                CONFIG_CHOOSER_BLUE_SIZE,
                CONFIG_CHOOSER_ALPHA_SIZE,
                CONFIG_CHOOSER_DEPTH_SIZE,
                CONFIG_CHOOSER_STENCIL_SIZE); // Alpha used for plane blending.
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

//        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.set("orientation", "portrait");
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.setPreviewSize(1280, 720);
//        mCamera.setDisplayOrientation(90)
//        setCameraDisplayOrientation(mActivity, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
//        mCamera.setParameters(parameters);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
