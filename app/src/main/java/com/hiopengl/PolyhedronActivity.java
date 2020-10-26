package com.hiopengl;

import javax.microedition.khronos.opengles.GL10;

public class PolyhedronActivity extends OpenGLActivity {

    @Override
    protected OpenGLActivity.GLRenderer getRenderer() {
        return new PolyhedronRenderer();
    }

    private class PolyhedronRenderer extends OpenGLActivity.GLRenderer {

        public PolyhedronRenderer() {
        }

        @Override
        void drawFrame(GL10 gl) {

        }
    }
}
