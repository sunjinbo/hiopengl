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
        float[] vertices = new float[3 * 6]; // 两个三角形8
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

        vertices[12] = 0f; // A
        vertices[13] = 0f;
        vertices[14] = 0f;

        vertices[15] = 0f; // C
        vertices[16] = -1f;
        vertices[17] = 0f;
        
        float[] barycentrics = new float[3 * 6];
        for (int i = 0; i < barycentrics.length; ++i) {
            if (i % 9 == 0 || i % 9 == 4 || i % 9 == 8) {
                barycentrics[i] = 1f;
            } else {
                barycentrics[i] = 0f;
            }
        }

        setData(vertices, barycentrics);
    }
}
