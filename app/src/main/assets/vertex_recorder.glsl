#version 300 es
layout (location = 0) in vec4 vPosition;

void main() {
     gl_Position  = vec4(vPosition.xyz, 1.0);
     gl_PointSize = vPosition.w;
}