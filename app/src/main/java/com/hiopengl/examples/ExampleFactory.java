package com.hiopengl.examples;

import android.text.TextUtils;

public class ExampleFactory {
    public static ExampleCategory getAllExamples() {
        ExampleCategory all = new ExampleCategory("all");

        ExampleCategory androidGraphicRendering = new ExampleCategory("Android Graphic Rendering");

        ExampleCategory viewAndRenderer = new ExampleCategory("View and Renderer");
        viewAndRenderer.addItem(new ExampleInfo("GLSurfaceView + OpenGL ES", "com.hiopengl.android.graphics.OpenGLGLSurfaceViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("TextureView + OpenGL ES", "com.hiopengl.android.graphics.OpenGLTextureViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("SurfaceView + OpenGL ES", "com.hiopengl.android.graphics.OpenGLSurfaceViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("View + Canvas", "com.hiopengl.android.graphics.CanvasViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("SurfaceView + Canvas", "com.hiopengl.android.graphics.CanvasSurfaceViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("TextureView + Canvas", "com.hiopengl.android.graphics.CanvasTextureViewActivity"));
        viewAndRenderer.addItem(new ExampleInfo("TextureView VS. SurfaceView", "com.hiopengl.android.graphics.SurfaceViewAndTextureViewActivity"));
        androidGraphicRendering.addItem(viewAndRenderer);

        ExampleCategory openGLTextureReader = new ExampleCategory("OpenGL Texture Reader");
        openGLTextureReader.addItem(new ExampleInfo("glReadPixels", "com.hiopengl.android.reader.GLReadPixelsActivity"));
        openGLTextureReader.addItem(new ExampleInfo("ImageReader", "com.hiopengl.android.reader.ImageReaderActivity"));
        openGLTextureReader.addItem(new ExampleInfo("OpenGL PBO", "com.hiopengl.android.reader.PBOActivity"));
        openGLTextureReader.addItem(new ExampleInfo("HardwareBuffer", ""));
        androidGraphicRendering.addItem(openGLTextureReader);

        ExampleCategory cameraWithOpenGLES = new ExampleCategory("Camera with OpenGL ES");
        cameraWithOpenGLES.addItem(new ExampleInfo("Camera Filter", "com.hiopengl.android.camera.CameraFilterActivity"));
        androidGraphicRendering.addItem(cameraWithOpenGLES);

        ExampleCategory screenRecorder = new ExampleCategory("Screen Recorder");
        screenRecorder.addItem(new ExampleInfo("Draw Twice", "com.hiopengl.android.recorder.DrawTwiceActivity"));
        screenRecorder.addItem(new ExampleInfo("glBlitFramebuffer", "com.hiopengl.android.recorder.GlBlitFramebufferActivity"));
        screenRecorder.addItem(new ExampleInfo("FBO blit x2", "com.hiopengl.android.recorder.FBOBlitx2Activity"));
        androidGraphicRendering.addItem(screenRecorder);

        ExampleCategory quickstartForAndroidARCore = new ExampleCategory("Quickstart for Android ARCore");
        quickstartForAndroidARCore.addItem(new ExampleInfo("ARCore Kickoff", ""));
        androidGraphicRendering.addItem(quickstartForAndroidARCore);

        all.addItem(androidGraphicRendering);

        ExampleCategory gettingStartedWithOpenGLES = new ExampleCategory("Getting Started with OpenGL® ES");

        ExampleCategory coordinateTransformations = new ExampleCategory("Coordinate Transformations");
        coordinateTransformations.addItem(new ExampleInfo("Modeling and Viewing", "com.hiopengl.basic.coordinate.ViewModelActivity"));
        coordinateTransformations.addItem(new ExampleInfo("Projection", "com.hiopengl.basic.coordinate.ProjectionActivity"));
        coordinateTransformations.addItem(new ExampleInfo("Viewport", "com.hiopengl.basic.coordinate.ViewportActivity"));
        gettingStartedWithOpenGLES.addItem(coordinateTransformations);

        gettingStartedWithOpenGLES.addItem(new ExampleInfo("Primitives", "com.hiopengl.basic.PrimitiveActivity"));

        ExampleCategory vertexData = new ExampleCategory("Vertex Data");
        vertexData.addItem(new ExampleInfo("Vertex Array for OpenGL ES 1.0", "com.hiopengl.basic.vertex.VertexArrayOpenGL10Activity"));
        vertexData.addItem(new ExampleInfo("Vertex Array for OpenGL ES 2.0", "com.hiopengl.basic.vertex.VertexArrayOpenGL20Activity"));
        vertexData.addItem(new ExampleInfo("Vertex Buffer for OpenGL ES 3.0", "com.hiopengl.basic.vertex.VertexBufferOpenGL30Activity"));
        gettingStartedWithOpenGLES.addItem(vertexData);

        ExampleCategory texture = new ExampleCategory("Texture");
        texture.addItem(new ExampleInfo("2D Texture", "com.hiopengl.basic.texture.Texture2DActivity"));
        texture.addItem(new ExampleInfo("3D Texture", "com.hiopengl.basic.texture.Texture3DActivity"));
        texture.addItem(new ExampleInfo("Mipmap Texture", "com.hiopengl.basic.texture.TextureMipmapActivity"));
        gettingStartedWithOpenGLES.addItem(texture);

        gettingStartedWithOpenGLES.addItem(new ExampleInfo("RBO(Render Buffer Object)", "com.hiopengl.basic.RBOActivity"));

        all.addItem(gettingStartedWithOpenGLES);

        ExampleCategory advancedTechnologyWithOpenGLES = new ExampleCategory("Advanced Technology with OpenGL® ES");
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Face culling", "com.hiopengl.advanced.FaceCullingActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Mesh", "com.hiopengl.advanced.MeshActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Frame Buffer", "com.hiopengl.advanced.FrameBufferActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Blending", "com.hiopengl.advanced.BlendingActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Stencil test", "com.hiopengl.advanced.StencilTestActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Depth test", "com.hiopengl.advanced.DepthTestActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Scissor test", "com.hiopengl.advanced.ScissorTestActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Lighting", "com.hiopengl.advanced.LightingActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Materials", "com.hiopengl.advanced.MaterialActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Shared Context and Texture", "com.hiopengl.advanced.SharedContextActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Assimp", "com.hiopengl.advanced.AssimpActivity"));
        advancedTechnologyWithOpenGLES.addItem(new ExampleInfo("Geometry Shader", "com.hiopengl.advanced.GeometryShaderActivity"));

        all.addItem(advancedTechnologyWithOpenGLES);

        ExampleCategory openGLESInPractice = new ExampleCategory("OpenGL® ES in Practice");
        openGLESInPractice.addItem(new ExampleInfo("Polyhedron", "com.hiopengl.practices.PolyhedronActivity"));
        openGLESInPractice.addItem(new ExampleInfo("Particle", "com.hiopengl.practices.ParticleActivity"));
        openGLESInPractice.addItem(new ExampleInfo("Text Rendering", "com.hiopengl.practices.TextRenderingActivity"));
        openGLESInPractice.addItem(new ExampleInfo("Skybox", "com.hiopengl.practices.SkyboxActivity"));

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
