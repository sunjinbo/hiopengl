#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 aColor;

out vec4 vColor;
uniform mat4 uMatrix;

void main() {
     gl_Position  = uMatrix * vPosition;
     vColor = aColor;
}
