package com.hiopengl.advanced.model;

import android.content.Context;

public class Pyramid extends Object3D {
    public Pyramid(Context context) {
        super(context);
        initVertex();
    }

    @Override
    public Mesh getType() {
        return Mesh.Pyramid;
    }

    private void initVertex() {
        float[] vertices = {
                // first triangle texture
                0.5f, -0.5f, -0.5f, // upper right corner
                0.5f, -0.5f, 0.5f, // lower right corner
                -0.5f, -0.5f, -0.5f, // upper left corner
                // second triangle
                0.5f, -0.5f, 0.5f, // lower right corner
                -0.5f, -0.5f, 0.5f, // lower left corner
                -0.5f, -0.5f, -0.5f, // upper left corner
                //The third triangle
                -0.5f, -0.5f, 0.5f, // lower left corner
                0.0f, 0.2f, 0.0f, // vertex
                0.5f, -0.5f, 0.5f, // lower right corner
                //fourth triangle
                0.5f, -0.5f, 0.5f, // lower right corner
                0.0f, 0.2f, 0.0f, // vertex
                0.5f, -0.5f, -0.5f, // upper right corner
                //The fifth triangle
                0.5f, -0.5f, -0.5f, // upper right corner
                0.0f, 0.2f, 0.0f, // vertex
                -0.5f, -0.5f, -0.5f, // upper left corner
                // sixth triangle
                -0.5f, -0.5f, -0.5f, // upper left corner
                0.0f, 0.2f, 0.0f, // vertex
                -0.5f, -0.5f, 0.5f // lower left corner
        };

        float[] bc = new float[vertices.length];
        for (int i = 0; i < bc.length; ++i) {
            if (i % 9 == 0 || i % 9 == 4 || i % 9 == 8) {
                bc[i] = 1f;
            } else {
                bc[i] = 0f;
            }
        }

        setData(vertices, bc);
    }
}
