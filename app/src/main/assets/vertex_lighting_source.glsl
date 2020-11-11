#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec4 aNormal;

out vec4 vColor;
out vec4 vNormal;
uniform mat4 vMatrix;

void main() {
     gl_Position  = vMatrix * vPosition;
     gl_PointSize = 100.0;
     vColor = aColor;
     vNormal = aNormal;
}