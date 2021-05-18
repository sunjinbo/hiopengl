package com.hiopengl.advanced;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;

import com.hiopengl.R;
import com.hiopengl.advanced.model.Cube;
import com.hiopengl.advanced.model.Cylinder;
import com.hiopengl.advanced.model.Mesh;
import com.hiopengl.advanced.model.Object3D;
import com.hiopengl.advanced.model.Plane;
import com.hiopengl.advanced.model.Pyramid;
import com.hiopengl.advanced.model.Sphere;
import com.hiopengl.advanced.model.Torus;
import com.hiopengl.base.ActionBarActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MeshActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    private Mesh mType = Mesh.Plane;
    private GLSurfaceView mGLSurfaceView;
    private Object3D mObject3D;
    private int mWidth;
    private int mHeight;

    private final float[] mModelMatrix = new float[16];
    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //ViewModel矩阵
    private final float[] mViewModelMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh);
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

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;

        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix,0,
                0,0, 3,// 摄像机坐标
                0f,0f,0f,// 目标物的中心坐标
                0f,1.0f,0.0f);// 相机方向

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewModelMatrix, 0);

        final float ratio = (float) mWidth / mHeight;
        Matrix.frustumM(mProjectMatrix,0, -ratio, ratio,-1f,1f, 1f,333f);
        Matrix.multiplyMM(mViewModelMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix,0, mProjectMatrix,0, mViewModelMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

        Object3D obj3d = getObject();
        if (obj3d != null) {
            Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0.5f, 0.5f, 0.0f);
            obj3d.draw(gl, mMVPMatrix);
        }
    }

    public void onPlaneClick(View view) {
        mType = Mesh.Plane;
    }

    public void onSphereClick(View view) {
        mType = Mesh.Sphere;
    }

    public void onCubeClick(View view) {
        mType = Mesh.Cube;
    }

    public void onCylinderClick(View view) {
        mType = Mesh.Cylinder;
    }

    public void onTorusClick(View view) {
        mType = Mesh.Torus;
    }

    public void onPyramidClick(View view) {
        mType = Mesh.Pyramid;
    }

    private Object3D getObject() {
        if (mObject3D == null || mObject3D.getType() != mType) {
            switch (mType) {
                case Plane:
                    mObject3D = new Plane(this,2, 2, 4, 4);
                    break;
                case Sphere:
                    mObject3D = new Sphere(this, 1f, 10, 10);
                    break;
                case Cube:
                    mObject3D = new Cube(this, 2);
                    break;
                case Cylinder:
                    mObject3D = new Cylinder(this, 1, 1, 10, 10);
                    break;
                case Pyramid:
                    mObject3D = new Pyramid(this);
                    break;
                case Torus:
                    mObject3D = new Torus(this, 0.8f, 0.4f, 20, 20);
                    break;
                default:
                    break;
            }
        }
        return mObject3D;
    }
}