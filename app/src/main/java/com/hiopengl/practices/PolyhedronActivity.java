package com.hiopengl.practices;

import android.content.Context;
import android.opengl.GLES30;
import android.os.Bundle;

import com.hiopengl.base.OpenGLActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class PolyhedronActivity extends OpenGLActivity {

    static final float X=.525731112119133606f;
    static final float Z=.850650808352039932f;
    static float vertices[] = new float[]{
            -0.6081204068324845f, 0.6081204068324845f, 0.510273594836914f,
            -0.35768269457782703f, 0.7153653891556541f, 0.600262817437266f,
            0.0f, 0.766044451956441f, 0.6427875991544609f,
            0.35768269457782703f, 0.7153653891556541f, 0.600262817437266f,
            0.6081204068324845f, 0.6081204068324845f, 0.510273594836914f,
            -0.7153653891556541f, 0.35768269457782703f, 0.600262817437266f,
            -0.45565995493042694f, 0.45565995493042694f, 0.764688178897518f,
            0.0f, 0.5118889480688306f, 0.8590516310705579f,
            0.45565995493042694f, 0.45565995493042694f, 0.764688178897518f,
            0.7153653891556541f, 0.35768269457782703f, 0.600262817437266f,
            -0.766044451956441f, 0.0f, 0.6427875991544609f,
            -0.5118889480688306f, 0.0f, 0.8590516310705579f,
            0.0f, 0.0f, 1.0f,
            0.5118889480688306f, 0.0f, 0.8590516310705579f,
            0.766044451956441f, 0.0f, 0.6427875991544609f,
            -0.7153653891556541f, -0.35768269457782703f, 0.600262817437266f,
            -0.45565995493042694f, -0.45565995493042694f, 0.764688178897518f,
            0.0f, -0.5118889480688306f, 0.8590516310705579f,
            0.45565995493042694f, -0.45565995493042694f, 0.764688178897518f,
            0.7153653891556541f, -0.35768269457782703f, 0.600262817437266f,
            -0.6081204068324845f, -0.6081204068324845f, 0.510273594836914f,
            -0.35768269457782703f, -0.7153653891556541f, 0.600262817437266f,
            0.0f, -0.766044451956441f, 0.6427875991544609f,
            0.35768269457782703f, -0.7153653891556541f, 0.600262817437266f,
            0.6081204068324845f, -0.6081204068324845f, 0.510273594836914f
    };
    static short indices[] = new short[]{
            0, 1, 5, 1, 6, 5, 1, 2, 6, 2, 7, 6, 2, 3, 7, 3, 8, 7, 3, 4, 8, 4, 9, 8, 5, 6, 10, 6, 11, 10, 6, 7, 11, 7, 12, 11, 7, 8, 12, 8, 13, 12, 8, 9, 13, 9, 14, 13, 10, 11, 15, 11, 16, 15, 11, 12, 16, 12, 17, 16, 12, 13, 17, 13, 18, 17, 13, 14, 18, 14, 19, 18, 15, 16, 20, 16, 21, 20, 16, 17, 21, 17, 22, 21, 17, 18, 22, 18, 23, 22, 18, 19, 23, 19, 24, 23
    };
    static float[] colors = {
            0f, 0f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 1f, 1f,
            1f, 0f, 0f, 1f,

            1f, 0f, 1f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,

            0f, 0f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 1f, 1f,
            1f, 0f, 0f, 1f,

            1f, 0f, 1f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,

            1f, 0f, 1f, 1f,
            1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected OpenGLActivity.GLRenderer getRenderer() {
        return new PolyhedronRenderer(this);
    }

    private class PolyhedronRenderer extends OpenGLActivity.GLRenderer {

        private FloatBuffer vertexBuffer;
        private FloatBuffer colorBuffer;
        private ShortBuffer indexBuffer;
        private float angle = 0.0f;

        public PolyhedronRenderer(Context context) {
            super(context);

            ByteBuffer vbb
                    = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);
            ByteBuffer cbb
                    = ByteBuffer.allocateDirect(colors.length * 4);
            cbb.order(ByteOrder.nativeOrder());
            colorBuffer = cbb.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);
            ByteBuffer ibb
                    = ByteBuffer.allocateDirect(indices.length * 2);
            ibb.order(ByteOrder.nativeOrder());
            indexBuffer = ibb.asShortBuffer();
            indexBuffer.put(indices);
            indexBuffer.position(0);
        }

        @Override
        public void drawFrame(GL10 gl) {
            gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                    | GL10.GL_DEPTH_BUFFER_BIT);

//            gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
//            gl.glLoadIdentity();
//            gl.glRotatef(angle, 1, 1, 1);
//            gl.glTranslatef(0f, 0f, 0f);

            gl.glFrontFace(GL10.GL_CCW);
            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glCullFace(GL10.GL_FRONT);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
                    GL10.GL_UNSIGNED_SHORT, indexBuffer);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisable(GL10.GL_CULL_FACE);
            angle++;
        }
    }
}
