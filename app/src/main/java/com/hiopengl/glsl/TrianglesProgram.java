package com.hiopengl.glsl;

import android.content.Context;

public class TrianglesProgram extends BaseProgram {
    private static float triangleCoords[] = {
            0.5f,  0.5f, 0.0f, // top
            0.0f, 0.5f, 0.0f, // bottom left
            -0.5f, -0.5f, 0.0f  // bottom right
    };

    private static float trianglesColors[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    public TrianglesProgram(Context context) {
        super(context, "vertex_glsl.glsl", "fragment_glsl.glsl");
    }

    @Override
    public float[] getCoordArray() {
        return triangleCoords;
    }

    @Override
    public float[] getColorArray() {
        return trianglesColors;
    }
}
