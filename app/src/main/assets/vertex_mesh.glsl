#version 300 es
layout (location = 0) in vec4 vPosition;

uniform mat4 vMatrix;

void main() {
     gl_Position  = vMatrix * vPosition;
     //gl_Position = vPosition;
     gl_PointSize = 5.0;
}