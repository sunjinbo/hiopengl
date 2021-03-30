package com.hiopengl.basic.vertex;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class VertexActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    protected static final float Coords[] ={
            0.5f,  0.5f, 0.5f, // A
            0.5f, 0.5f, -0.5f, // B

            0.5f, 0.5f, -0.5f, // B
            0.5f, -0.5f, -0.5f, // C

            0.5f, -0.5f, -0.5f, // C
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

    protected static final float Color[] = {
            0.49f, 0.73f, 0.91f, 1.0f, // A
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.77f, 0.12f, 0.23f, 1.0f, // C
            0.77f, 0.12f, 0.23f, 1.0f, // C
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.49f, 0.73f, 0.91f, 1.0f, // A
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.49f, 0.73f, 0.91f, 1.0f, // A
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.59f, 0.44f, 0.09f, 1.0f, // D
            0.0f, 0.1f, 0.0f, 1.0f, // G
            0.01f, 0.28f, 1.0f, 1.0f, // B
            0.0f, 0.1f, 0.0f, 1.0f, // G
            1.0f, 0.0f, 0.0f, 1.0f, // F
            0.0f, 0.1f, 0.0f, 1.0f, // G
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.0f, 1.0f, 0.25f, 1.0f, // E
            0.64f, 0.0f, 0.43f, 1.0f, // H
            0.77f, 0.12f, 0.23f, 1.0f // C
    };

    // Flat Color
    protected float[] rgba = new float[]{1.0f, 0.0f, 1.0f, 1.0f};

    protected GLSurfaceView mGLSurfaceView;
    protected FloatBuffer mPositionFloatBuffer;
    protected FloatBuffer mColorFloatBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertex);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
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

    protected void initVertexBuffer() {
        ByteBuffer posByteBuffer = ByteBuffer.allocateDirect(Coords.length * 4);
        posByteBuffer.order(ByteOrder.nativeOrder());
        mPositionFloatBuffer = posByteBuffer.asFloatBuffer();
        mPositionFloatBuffer.put(Coords);
        mPositionFloatBuffer.position(0);

        ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(Color.length * 4);
        colorByteBuffer.order(ByteOrder.nativeOrder());
        mColorFloatBuffer = colorByteBuffer.asFloatBuffer();
        mColorFloatBuffer.put(Color);
        mColorFloatBuffer.position(0);
    }
}
