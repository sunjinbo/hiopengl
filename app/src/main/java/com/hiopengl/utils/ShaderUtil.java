package com.hiopengl.utils;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import java.io.IOException;
import java.io.InputStream;

public class ShaderUtil {
    private static int sVersion = 3;

    public static void setEGLContextClientVersion(int version) {
        if (version == 2 || version == 3) {
            sVersion = version;
        }
    }

    /**
     * 编译顶点着色器
     *
     * @param shaderCode
     * @return
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES30.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode
     * @return
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译
     *
     * @param type       顶点着色器:GLES30.GL_VERTEX_SHADER
     *                   片段着色器:GLES30.GL_FRAGMENT_SHADER
     * @param shaderCode
     * @return
     */
    private static int compileShader(int type, String shaderCode) {
        //创建一个着色器
        final int shaderId = GLES.glCreateShader(type);
        if (shaderId != 0) {
            GLES.glShaderSource(shaderId, shaderCode);
            GLES.glCompileShader(shaderId);
            //检测状态
            final int[] compileStatus = new int[1];
            GLES.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                String logInfo = GLES.glGetShaderInfoLog(shaderId);
                System.err.println(logInfo);
                //创建失败
                GLES.glDeleteShader(shaderId);
                return 0;
            }
            return shaderId;
        } else {
            //创建失败
            return 0;
        }
    }

    /**
     * 链接小程序
     *
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     * @return
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = GLES.glCreateProgram();
        if (programId != 0) {
            //将顶点着色器加入到程序
            GLES.glAttachShader(programId, vertexShaderId);
            //将片元着色器加入到程序中
            GLES.glAttachShader(programId, fragmentShaderId);
            //链接着色器程序
            GLES.glLinkProgram(programId);
            final int[] linkStatus = new int[1];

            GLES.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String logInfo = GLES.glGetProgramInfoLog(programId);
                System.err.println(logInfo);
                GLES.glDeleteProgram(programId);
                return 0;
            }
            return programId;
        } else {
            //创建失败
            return 0;
        }
    }

    /**
     * 验证程序片段是否有效
     *
     * @param programObjectId
     * @return
     */
    public static boolean validProgram(int programObjectId) {
        GLES.glValidateProgram(programObjectId);
        final int[] programStatus = new int[1];
        GLES.glGetProgramiv(programObjectId, GLES30.GL_VALIDATE_STATUS, programStatus, 0);
        return programStatus[0] != 0;
    }

    public static String loadAssets(Context context, String fileName) {
        String result = null;
        try {
            InputStream is = context.getResources().getAssets()
                    .open(fileName);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            result = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static class GLES {
        public static int glCreateShader(int type) {
            if (sVersion == 2) {
                return GLES20.glCreateShader(type);
            } else {
                return GLES30.glCreateShader(type);
            }
        }

        public static int glCreateProgram() {
            if (sVersion == 2) {
                return GLES20.glCreateProgram();
            } else {
                return GLES30.glCreateProgram();
            }
        }

        public static void glShaderSource(int shader, String string) {
            if (sVersion == 2) {
                GLES20.glShaderSource(shader, string);
            } else {
                GLES30.glShaderSource(shader, string);
            }
        }

        public static void glCompileShader(int shader) {
            if (sVersion == 2) {
                GLES20.glCompileShader(shader);
            } else {
                GLES30.glCompileShader(shader);
            }
        }

        public static void glGetShaderiv(int shader,
                                         int pname,
                                         int[] params,
                                         int offset) {
            if (sVersion == 2) {
                GLES20.glGetShaderiv(shader, pname, params, offset);
            } else {
                GLES30.glGetShaderiv(shader, pname, params, offset);
            }
        }

        public static void glDeleteShader(int shader) {
            if (sVersion == 2) {
                GLES20.glDeleteShader(shader);
            } else {
                GLES30.glDeleteShader(shader);
            }
        }

        public static void glDeleteProgram(int program) {
            if (sVersion == 2) {
                GLES20.glDeleteProgram(program);
            } else {
                GLES30.glDeleteProgram(program);
            }
        }

        public static void glAttachShader(int program,
                                          int shader) {
            if (sVersion == 2) {
                GLES20.glAttachShader(program, shader);
            } else {
                GLES30.glAttachShader(program, shader);
            }
        }

        public static void glLinkProgram(int program) {
            if (sVersion == 2) {
                GLES20.glLinkProgram(program);
            } else {
                GLES30.glLinkProgram(program);
            }
        }

        public static void glGetProgramiv(int program,
                                          int pname,
                                          int[] params,
                                          int offset) {
            if (sVersion == 2) {
                GLES20.glGetProgramiv(program, pname, params, offset);
            } else {
                GLES30.glGetProgramiv(program, pname, params, offset);
            }
        }

        public static void glValidateProgram(int program) {
            if (sVersion == 2) {
                GLES20.glValidateProgram(program);
            } else {
                GLES30.glValidateProgram(program);
            }
        }

        public static String glGetShaderInfoLog(int shader) {
            if (sVersion == 2) {
                return GLES20.glGetShaderInfoLog(shader);
            } else {
                return GLES30.glGetShaderInfoLog(shader);
            }
        }

        public static String glGetProgramInfoLog(int program) {
            if (sVersion == 2) {
                return GLES20.glGetProgramInfoLog(program);
            } else {
                return GLES30.glGetProgramInfoLog(program);
            }
        }
    }
}
