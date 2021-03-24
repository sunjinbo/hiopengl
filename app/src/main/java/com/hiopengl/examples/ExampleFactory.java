package com.hiopengl.examples;

import android.text.TextUtils;

public class ExampleFactory {
    public static ExampleCategory getAllExamples() {
        ExampleCategory all = new ExampleCategory("all");

        ExampleCategory androidGraphicRendering = new ExampleCategory("Android Graphic Rendering");

        ExampleCategory viewAndRenderer = new ExampleCategory("View and Renderer");
        viewAndRenderer.addItem(new ExampleInfo("GLSurfaceView + OpenGL ES", ""));
        viewAndRenderer.addItem(new ExampleInfo("TextureView + OpenGL ES", ""));
        viewAndRenderer.addItem(new ExampleInfo("SurfaceView + OpenGL ES", ""));
        viewAndRenderer.addItem(new ExampleInfo("SurfaceTexture + OpenGL ES", ""));
        viewAndRenderer.addItem(new ExampleInfo("View + Canvas", ""));
        viewAndRenderer.addItem(new ExampleInfo("SurfaceView + Canvas", ""));
        viewAndRenderer.addItem(new ExampleInfo("TextureView + Canvas", ""));
        viewAndRenderer.addItem(new ExampleInfo("TextureView VS. SurfaceView", ""));
        androidGraphicRendering.addItem(viewAndRenderer);

        ExampleCategory openGLTextureReader = new ExampleCategory("OpenGL Texture Reader");
        openGLTextureReader.addItem(new ExampleInfo("glReadPixels", ""));
        openGLTextureReader.addItem(new ExampleInfo("ImageReader", ""));
        openGLTextureReader.addItem(new ExampleInfo("OpenGL PBO", ""));
        openGLTextureReader.addItem(new ExampleInfo("HardwareBuffer", ""));
        androidGraphicRendering.addItem(openGLTextureReader);

        ExampleCategory cameraWithOpenGLES = new ExampleCategory("Camera with OpenGL ES");
        androidGraphicRendering.addItem(cameraWithOpenGLES);

        ExampleCategory screenRecorder = new ExampleCategory("Screen Recorder");
        screenRecorder.addItem(new ExampleInfo("Draw Twice", ""));
        screenRecorder.addItem(new ExampleInfo("glBlitFramebuffer", ""));
        screenRecorder.addItem(new ExampleInfo("FBO blitx2", ""));
        androidGraphicRendering.addItem(screenRecorder);

        ExampleCategory quickstartForAndroidARCore = new ExampleCategory("Quickstart for Android ARCore");
        androidGraphicRendering.addItem(quickstartForAndroidARCore);

        all.addItem(androidGraphicRendering);

        ExampleCategory gettingStartedWithOpenGLES = new ExampleCategory("Getting Started with OpenGL® ES");

        ExampleCategory coordinateTransformations = new ExampleCategory("Coordinate Transformations");
        coordinateTransformations.addItem(new ExampleInfo("Modeling and Viewing", ""));
        coordinateTransformations.addItem(new ExampleInfo("Projection", ""));
        coordinateTransformations.addItem(new ExampleInfo("Viewport", ""));
        gettingStartedWithOpenGLES.addItem(coordinateTransformations);

        gettingStartedWithOpenGLES.addItem(new ExampleInfo("Geometric Figures", ""));

        ExampleCategory vertexData = new ExampleCategory("Vertex Data");
        vertexData.addItem(new ExampleInfo("Vertex Array for OpenGL ES 1.0", ""));
        vertexData.addItem(new ExampleInfo("Vertex Array for OpenGL ES 2.0", ""));
        vertexData.addItem(new ExampleInfo("Vertex Buffer for OpenGL ES 3.0", ""));
        gettingStartedWithOpenGLES.addItem(vertexData);

        gettingStartedWithOpenGLES.addItem(new ExampleInfo("2D/3D Texture", ""));
        gettingStartedWithOpenGLES.addItem(new ExampleInfo("RBO(Render Buffer Object)", ""));

        all.addItem(gettingStartedWithOpenGLES);

        ExampleCategory advancedTechnologyWithOpenGLES = new ExampleCategory("Advanced Technology with OpenGL® ES");
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Face culling", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Mesh", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Frame Buffer", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Blending", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Stencil test", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Depth test", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Scissor test", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Lighting", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Materials", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Shared Context and Texture", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Model Loading", ""));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Assimp", ""));

        all.addItem(advancedTechnologyWithOpenGLES);

        ExampleCategory openGLESInPractice = new ExampleCategory("OpenGL® ES in Practice");
        openGLESInPractice.addItem(new ExampleInfo("Polyhedron", ""));
        openGLESInPractice.addItem(new ExampleInfo("Particles", ""));
        openGLESInPractice.addItem(new ExampleInfo("Text Rendering", ""));
        openGLESInPractice.addItem(new ExampleInfo("Skybox", ""));

        all.addItem(openGLESInPractice);

        return all;
    }

    public static ExampleCategory getParentCategory(ExampleCategory root, ExampleItem item) {
        if (TextUtils.equals(root.getExampleName(), item.getExampleName())) return null;

        for (ExampleItem it : root.getItems()) {
            if (TextUtils.equals(it.getExampleName(), item.getExampleName())) return root;

            if (it instanceof ExampleCategory) {
                ExampleCategory find = getParentCategory((ExampleCategory)it, item);
                if (find != null) {
                    return find;
                }
            }
        }

        return null;
    }
}
