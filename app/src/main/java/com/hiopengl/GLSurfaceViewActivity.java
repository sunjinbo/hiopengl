package com.hiopengl;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceViewActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private GLRenderer mGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurfaceview);

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

    private class GLRenderer implements GLSurfaceView.Renderer {

        float[] vertexArray = new float[]{
                -0.8f , -0.4f * 1.732f , 0.0f ,
                0.8f , -0.4f * 1.732f , 0.0f ,
                0.0f , 0.4f * 1.732f , 0.0f ,
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
            gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
            gl.glPointSize(18f);
            gl.glLoadIdentity();
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex);
            gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 3);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }
}