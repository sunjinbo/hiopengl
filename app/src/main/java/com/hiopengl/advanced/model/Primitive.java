package com.hiopengl.advanced.model;

import android.content.Context;

public class Primitive extends Object3D {
    public Primitive(Context context) {
        super(context);
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Primitive;
    }

    private void initVertex() {
        float[] vertices = new float[3 * 4]; // 两个三角形8
        vertices[0] = 0f; // A
        vertices[1] = 0f;
        vertices[2] = 0f;

        vertices[3] = -1f; // B
        vertices[4] = 0f;
        vertices[5] = 0f;

        vertices[6] = 0f; // C
        vertices[7] = -1f;
        vertices[8] = 0f;

        vertices[9] = 1f; // D
        vertices[10] = 0f;
        vertices[11] = 0f;

        short[] indices = new short[6];
        indices[0] = 0;
        indices[1] = 1;
        indices[2] = 2;

        indices[3] = 0;
        indices[4] = 2;
        indices[5] = 3;

        setData(vertices, indices);
    }
}
