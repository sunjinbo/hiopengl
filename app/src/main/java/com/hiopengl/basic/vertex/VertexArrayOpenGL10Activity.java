package com.hiopengl.basic.vertex;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VertexArrayOpenGL10Activity extends ActionBarActivity implements GLSurfaceView.Renderer {

    private static final float Coords[] ={
            0.5f,  0.5f, 0.5f, // A
            0.5f, 0.5f, -0.5f, // B

            0.5f, 0.5f, -0.5f, // B
            0.5f, -0.5f, -0.5f,  // C

            0.5f, -0.5f, -0.5f,  // C
            0.5f, -0.5f, 0.5f,  // D

            0.5f, -0.5f, 0.5f,  // D
            0.5f,  0.5f, 0.5f, // A

            -0.5f, 0.5f, 0.5f,  // F
            0.5f,  0.5f, 0.5f, // A

            -0.5f, 0.5f, 0.5f,  // F
            -0.5f, -0.5f, 0.5f,  // E

            -0.5f, -0.5f, 0.5f,  // E
            0.5f, -0.5f, 0.5f,  // D

            -0.5f, 0.5f, -0.5f,  // G
            0.5f, 0.5f, -0.5f, // B

            -0.5f, 0.5f, -0.5f,  // G
            -0.5f, 0.5f, 0.5f,  // F

            -0.5f, 0.5f, -0.5f,  // G
            -0.5f, -0.5f, -0.5f,  // H

            -0.5f, -0.5f, -0.5f,  // H
            -0.5f, -0.5f, 0.5f,  // E

            -0.5f, -0.5f, -0.5f,  // H
            0.5f, -0.5f, -0.5f  // C
    };

    private static final float Color[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f
    };

    // Flat Color
    private float[] rgba = new float[]{1.0f, 0.0f, 1.0f, 1.0f};

    private GLSurfaceView mGLSurfaceView;
    private FloatBuffer mPositionFloatBuffer;
    private FloatBuffer mColorFloatBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertex_array_opengl10);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        ByteBuffer posByteBuffer = ByteBuffer.allocateDirect(Coords.length * 4);
        posByteBuffer.order(ByteOrder.nativeOrder());
        mPositionFloatBuffer = posByteBuffer.asFloatBuffer();
        mPositionFloatBuffer.put(Coords);
        mPositionFloatBuffer.position(0);

//        ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(Color.length * 4);
//        colorByteBuffer.order(ByteOrder.nativeOrder());
//        mColorFloatBuffer = colorByteBuffer.asFloatBuffer();
//        mColorFloatBuffer.put(Color);
//        mColorFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mPositionFloatBuffer);

        gl.glRotatef(0.5f, 0.5f, 0.5f, 0.0f);

        // Set flat color
        gl.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

        // Smooth color
        if (mColorFloatBuffer != null ) {
            // Enable the color array buffer to be used during rendering.
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            // Point out the where the color buffer is.
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorFloatBuffer);
        }

        gl.glDrawArrays(GL10.GL_LINES, 0, 24);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}