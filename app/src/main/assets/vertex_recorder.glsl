#version 300 es
layout (location = 0) in vec4 vPosition;

void main() {
     gl_PointSize = vPosition.w;
     gl_Position  = vec4(vPosition.xyz, 1.0);
}