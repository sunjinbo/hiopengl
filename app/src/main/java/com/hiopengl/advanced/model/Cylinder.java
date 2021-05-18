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
        float[] vertices = new float[numVertices * 3];
        int i, j;
        int vertIndex = 0, index = 0;

        for (j = 0; j <= mSegmentsL; ++j) {
            float z = mLength * ((float)j/(float)mSegmentsL) - mLength / 2.0f;

            for (i = 0; i <= mSegmentsC; ++i) {
                float verAngle = 2.0f * PI * i / mSegmentsC;
                float x = mRadius * (float) Math.cos(verAngle);
                float y = mRadius * (float) Math.sin(verAngle);

                vertices[vertIndex++] = x;
                vertices[vertIndex++] = y;
                vertices[vertIndex++] = z;
            }
        }
        mNumVertices = numVertices;
        setData(vertices);
    }
}
