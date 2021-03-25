package com.hiopengl.advanced;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FaceCullingActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mGLRenderer;

    private int mFrontFace = GL10.GL_CCW; // 逆时针的顶点组成的三角形为正面
    private int mCullFace = GL10.GL_BACK; // 默认选择剔除背面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_culling);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLRenderer = new GLRenderer();
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

    public void onBackClick(View view) {
        mCullFace = GL10.GL_BACK;
    }

    public void onFrontClick(View view) {
        mCullFace = GL10.GL_FRONT;
    }

    public void onFrontAndBackClick(View view) {
        mCullFace = GL10.GL_FRONT_AND_BACK;
    }

    public void onCCWClick(View view) {
        mFrontFace = GL10.GL_CCW;
    }

    public void onCWClick(View view) {
        mFrontFace = GL10.GL_CW;
    }

    private class GLRenderer implements GLSurfaceView.Renderer {

        float[] vertexArray = new float[]{
                0.5f , 0.5f , 0.0f,
                0.0f , 0.5f , 0.0f,
                0.0f , 0.0f , 0.0f
        };

        ByteBuffer vbb;
        FloatBuffer vertex;

        public GLRenderer() {
            vbb = ByteBuffer.allocateDirect(vertexArray.length*4);
            vbb.order(ByteOrder.nativeOrder());
            vertex = vbb.asFloatBuffer();
            vertex.put(vertexArray);
            vertex.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            // Clears the screen and depth buffer.
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glCullFace(mCullFace);
            gl.glFrontFace(mFrontFace);
            gl.glPointSize(18f);
            gl.glLoadIdentity();
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex);
            gl.glColor4f(0.0f, 1.0f, 1.0f, 1.0f);
            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
            gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
            gl.glDrawArrays(GL10.GL_POINTS, 0, 5);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
}
