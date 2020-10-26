package com.hiopengl;

import javax.microedition.khronos.opengles.GL10;

public class GeometricActivity extends OpenGLActivity {

    @Override
    protected GLRenderer getRenderer() {
        return new GeometricRenderer();
    }

    private class GeometricRenderer extends OpenGLActivity.GLRenderer {

        public GeometricRenderer() {
        }

        @Override
        void drawFrame(GL10 gl) {

        }
    }
}
