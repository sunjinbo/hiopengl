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

        int numIndices = 2 * mSegmentsW * (mSegmentsH - 1) * 3;
        short[] indices = new short[numIndices];
        int index = 0;

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

                if(indices.length==0) continue;

                if (i > 0 && j > 0) {
                    short a = (short)((mSegmentsW + 1) * j + i);
                    short b = (short)(((mSegmentsW + 1) * j + i - 1));
                    short c = (short)(((mSegmentsW + 1) * (j - 1) + i - 1));
                    short d = (short)(((mSegmentsW + 1) * (j - 1) + i));

                    if (j == mSegmentsH) {
                        indices[index++] = a;
                        indices[index++] = c;
                        indices[index++] = d;
                    } else if (j == 1) {
                        indices[index++] = a;
                        indices[index++] = b;
                        indices[index++] = c;
                    } else {
                        indices[index++] = a;
                        indices[index++] = b;
                        indices[index++] = c;
                        indices[index++] = a;
                        indices[index++] = c;
                        indices[index++] = d;
                    }
                }
            }
        }

        mVertexSize = numVertices;

        setData(vertices, indices);
    }
}
