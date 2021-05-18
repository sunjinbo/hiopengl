package com.hiopengl.advanced.model;

import android.content.Context;

public class Torus extends Object3D {

    private final float PI = (float) Math.PI;
    private float mLargeRadius;
    private float mSmallRadius;
    private int mSegmentsL;
    private int mSegmentsS;

    public Torus(Context context, float largeRadius, float smallRadius, int segmentsL, int segmentsS) {
        super(context);
        mLargeRadius = largeRadius;
        mSmallRadius = smallRadius;
        mSegmentsL = segmentsL;
        mSegmentsS = segmentsS;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Torus;
    }

    private void initVertex() {
        int numVertices = (mSegmentsS + 1) * (mSegmentsL + 1);
        int numIndices = 2 * mSegmentsS * mSegmentsL * 3;

        float[] vertices = new float[numVertices * 3];
        short[] indices = new short[numIndices];

        int i, j;
        int vertIndex = 0, index = 0;
        final float normLen = 1.0f / mSmallRadius;

        for (j = 0; j <= mSegmentsL; ++j) {
            float largeRadiusAngle = 2.0f * PI * j / mSegmentsL;

            for (i = 0; i <= mSegmentsS; ++i) {
                float smallRadiusAngle = 2.0f * PI * i / mSegmentsS;
                float xNorm = (mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.sin(largeRadiusAngle);
                float x = (mLargeRadius + mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.sin(largeRadiusAngle);
                float yNorm = (mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.cos(largeRadiusAngle);
                float y = (mLargeRadius + mSmallRadius * (float) Math.sin(smallRadiusAngle)) * (float) Math.cos(largeRadiusAngle);
                float zNorm = mSmallRadius * (float) Math.cos(smallRadiusAngle);
                float z = zNorm;
                vertices[vertIndex++] = x;
                vertices[vertIndex++] = y;
                vertices[vertIndex++] = z;

                if (i > 0 && j > 0) {
                    int a = (mSegmentsS + 1) * j + i;
                    int b = (mSegmentsS + 1) * j + i - 1;
                    int c = (mSegmentsS + 1) * (j - 1) + i - 1;
                    int d = (mSegmentsS + 1) * (j - 1) + i;

                    indices[index++] = (short)a;
                    indices[index++] = (short)c;
                    indices[index++] = (short)b;
                    indices[index++] = (short)a;
                    indices[index++] = (short)d;
                    indices[index++] = (short)c;
                }
            }
        }

        setData(vertices, indices);
    }
}
