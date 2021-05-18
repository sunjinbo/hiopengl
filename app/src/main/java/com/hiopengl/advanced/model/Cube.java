package com.hiopengl.advanced.model;

import android.content.Context;

public class Cube extends Object3D {
    private int mSize;

    public Cube(Context context, int size) {
        super(context);
        mSize = size;
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Cube;
    }

    private void initVertex() {
        float halfSize = mSize * .5f;
        float[] vertices = {
                halfSize, halfSize, halfSize, 			-halfSize, halfSize, halfSize,
                -halfSize, -halfSize, halfSize,			halfSize, -halfSize, halfSize, // 0-1-2-3 front

                halfSize, halfSize, -halfSize,			 halfSize, halfSize, halfSize,
                halfSize, -halfSize, halfSize,			 halfSize, -halfSize, -halfSize, // 5-0-3-4 right

                -halfSize, halfSize, -halfSize,			halfSize, halfSize, -halfSize,
                halfSize, -halfSize, -halfSize, 		-halfSize, -halfSize, -halfSize, // 6-5-4-7 back

                -halfSize, halfSize, halfSize, 			-halfSize, halfSize, -halfSize,
                -halfSize, -halfSize, -halfSize,		-halfSize,-halfSize, halfSize, // 1-6-7-2 left

                -halfSize, halfSize, halfSize,			 halfSize, halfSize, halfSize,
                halfSize, halfSize, -halfSize,			-halfSize, halfSize, -halfSize,  // 1-0-5-6 top

                halfSize, -halfSize, halfSize, 			-halfSize, -halfSize, halfSize,
                -halfSize, -halfSize, -halfSize,		halfSize, -halfSize, -halfSize, // 3-2-7-4 bottom
        };
        mNumVertices = 24;
        setData(vertices);
    }
}
