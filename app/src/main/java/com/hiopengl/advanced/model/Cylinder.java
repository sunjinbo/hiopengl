package com.hiopengl.advanced.model;

import android.content.Context;

public class Cylinder extends Object3D {
    private final float PI = (float) Math.PI;
    private float mLength;
    private float mRadius;
    private int mSegmentsC;
    private int mSegmentsL;

    public Cylinder(Context context, float length, float radius, int segmentsL, int segmentsC) {
        super(context);
        mLength = length;
        mRadius = radius;
        mSegmentsL = segmentsL;
        mSegmentsC = segmentsC;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Cylinder;
    }

    private void initVertex() {
        int numVertices = (mSegmentsC + 1) * (mSegmentsL + 1);
        int numIndices = 2 * mSegmentsC * mSegmentsL * 3;

        float[] vertices = new float[numVertices * 3];
        short[] indices = new short[numIndices];

        int i, j;
        int vertIndex = 0, index = 0;
        final float normLen = 1.0f / mRadius;

        for (j = 0; j <= mSegmentsL; ++j) {
            float z = mLength * ((float)j/(float)mSegmentsL) - mLength / 2.0f;

            for (i = 0; i <= mSegmentsC; ++i) {
                float verAngle = 2.0f * PI * i / mSegmentsC;
                float x = mRadius * (float) Math.cos(verAngle);
                float y = mRadius * (float) Math.sin(verAngle);

                vertices[vertIndex++] = x;
                vertices[vertIndex++] = y;
                vertices[vertIndex++] = z;
                if (i > 0 && j > 0) {
                    int a = (mSegmentsC + 1) * j + i;
                    int b = (mSegmentsC + 1) * j + i - 1;
                    int c = (mSegmentsC + 1) * (j - 1) + i - 1;
                    int d = (mSegmentsC + 1) * (j - 1) + i;
                    indices[index++] = (short)a;
                    indices[index++] = (short)b;
                    indices[index++] = (short)c;
                    indices[index++] = (short)a;
                    indices[index++] = (short)c;
                    indices[index++] = (short)d;
                }
            }
        }

        setData(vertices, indices);
    }
}
