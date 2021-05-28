package com.hiopengl.advanced;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.hiopengl.R;
import com.hiopengl.base.ActionBarActivity;
import com.hiopengl.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GeometryShaderActivity extends ActionBarActivity {

    private GLSurfaceView mGLSurfaceView;
    private GeometryShaderRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geometry_shader);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mRenderer = new GeometryShaderRenderer(this);
        mGLSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private class GeometryShaderRenderer implements GLSurfaceView.Renderer {

        private Context mContext;
        private int mProgram;

        private FloatBuffer vertexBuffer;
        private float vertex[] = {
                -0.5f,  0.5f, // 左上方
                0.5f,  0.5f,  // 右上方
                0.5f, -0.5f,  // 右下方
                -0.5f, -0.5f  // 左下方
        };

        public GeometryShaderRenderer(Context context) {
            mContext = context;

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuffer.asFloatBuffer();
            vertexBuffer.put(vertex);
            vertexBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            initProgram();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES32.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES32.glClearColor(0.2F, 0.5F, 0.7F, 1.0F);
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT
                    | GLES32.GL_DEPTH_BUFFER_BIT);

            GLES32.glUseProgram(mProgram);

            int aPositionLocation = GLES30.glGetAttribLocation(mProgram,"position");
            GLES30.glEnableVertexAttribArray(aPositionLocation);
            GLES30.glVertexAttribPointer(aPositionLocation,2, GLES30.GL_FLOAT,false,0, vertexBuffer);

            GLES32.glDrawArrays(GLES32.GL_POINTS, 0, 4);

            GLES30.glDisableVertexAttribArray(aPositionLocation);
        }

        private void initProgram() {
            //编译顶点着色程序
            String vertexShaderStr = ShaderUtil.loadAssets(mContext, "vertex_geometry_shader.glsl");
            int vertexShaderId = compileVertexShader(vertexShaderStr);

            //编译几何着色程序
            String geometryShaderStr = ShaderUtil.loadAssets(mContext, "geometry_geometry_shader.glsl");
            int geometryShaderId = compileGeometryShader(geometryShaderStr);

            //编译片段着色程序
            String fragmentShaderStr = ShaderUtil.loadAssets(mContext, "fragment_geometry_shader.glsl");
            int fragmentShaderId = compileFragmentShader(fragmentShaderStr);

            //链接程序
            mProgram = linkProgram(vertexShaderId, geometryShaderId, fragmentShaderId);
            //在OpenGLES环境中使用程序
            GLES32.glUseProgram(mProgram);
        }

        // 编译顶点着色器
        private int compileVertexShader(String shaderCode) {
            return compileShader(GLES32.GL_VERTEX_SHADER, shaderCode);
        }

        // 编译几何着色器
        public int compileGeometryShader(String shaderCode) {
            return compileShader(GLES32.GL_GEOMETRY_SHADER, shaderCode);
        }

        // 编译片段着色器
        public int compileFragmentShader(String shaderCode) {
            return compileShader(GLES32.GL_FRAGMENT_SHADER, shaderCode);
        }

        // 编译着色器
        private int compileShader(int type, String shaderCode) {
            //创建一个新的着色器
            final int shaderId = GLES32.glCreateShader(type);
            if (shaderId != 0) {
                GLES32.glShaderSource(shaderId, shaderCode);
                GLES32.glCompileShader(shaderId);
                //检测状态
                final int[] compileStatus = new int[1];
                GLES32.glGetShaderiv(shaderId, GLES32.GL_COMPILE_STATUS, compileStatus, 0);
                if (compileStatus[0] == 0) {
                    String logInfo = GLES32.glGetShaderInfoLog(shaderId);
                    System.err.println(logInfo);
                    GLES32.glDeleteShader(shaderId); // 创建着色器失败
                    return 0;
                }
                return shaderId;
            } else {
                return 0; // 创建着色器失败
            }
        }

        // 编译链接Shader程序
        public int linkProgram(int vertexShader, int geometryShader, int fragmentShader) {
            final int program = GLES32.glCreateProgram();
            if (program != 0) {
                // 将顶点着色器加入到程序
                GLES32.glAttachShader(program, vertexShader);
                // 将几何着色器加入到程序
                GLES32.glAttachShader(program, geometryShader);
                // 将片元着色器加入到程序中
                GLES32.glAttachShader(program, fragmentShader);
                // 链接着色器程序
                GLES32.glLinkProgram(program);
                final int[] linkStatus = new int[1];

                GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] == 0) {
                    String logInfo = GLES32.glGetProgramInfoLog(program);
                    System.err.println(logInfo);
                    GLES32.glDeleteProgram(program);
                    return 0;
                }
                return program;
            } else {
                return 0;
            }
        }
    }
}
