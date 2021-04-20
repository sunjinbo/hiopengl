package com.hiopengl.advanced.model;

import android.content.Context;

public class Plane extends Object3D {
    protected float mWidth;
    protected float mHeight;
    protected int mSegmentsW;
    protected int mSegmentsH;

    public Plane(Context context, float width, float height, int segmentsW, int segmentsH) {
        super(context);
        mWidth = width;
        mHeight = height;
        mSegmentsW = segmentsW;
        mSegmentsH = segmentsH;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Plane;
    }

    private void initVertex() {
        int i, j;
        mVertexSize = (mSegmentsW + 1) * (mSegmentsH + 1);
        float[] vertices = new float[mVertexSize * 3];
        int vertexCount = 0;

        for (i = 0; i <= mSegmentsW; i++) {
            for (j = 0; j <= mSegmentsH; j++) {
                float v1 = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
                float v2 = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
                vertices[vertexCount] = v1;
                vertices[vertexCount + 1] = v2;
                vertices[vertexCount + 2] = 0;

                vertexCount += 3;
            }
        }

        setData(vertices);
    }
}
