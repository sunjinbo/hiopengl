package com.hiopengl.advanced.model;

import android.content.Context;

public class Sphere extends Object3D {
    private final float mRadius;
    private final int mSegmentsW;
    private final int mSegmentsH;

    public Sphere(Context context, float radius, int segmentsW, int segmentsH) {
        super(context);
        mRadius = radius;
        mSegmentsW = segmentsW;
        mSegmentsH = segmentsH;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Sphere;
    }

    private void initVertex() {
        int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);

        if(numVertices < 0) return;

        float[] vertices = new float[numVertices * 3];

        int i, j;
        int vertIndex = 0;

        for (j = 0; j <= mSegmentsH; ++j) {
            float horAngle = (float) (Math.PI * j / mSegmentsH);
            float z = mRadius * (float) Math.cos(horAngle);
            float ringRadius = mRadius * (float) Math.sin(horAngle);

            for (i = 0; i <= mSegmentsW; ++i) {
                float verAngle = (float) (2.0f * Math.PI * i / mSegmentsW);
                float x = ringRadius * (float) Math.cos(verAngle);
                float y = ringRadius * (float) Math.sin(verAngle);

                vertices[vertIndex++] = x;
                vertices[vertIndex++] = z;
                vertices[vertIndex++] = y;
            }
        }

        mVertexSize = numVertices;

        setData(vertices);
    }
}
