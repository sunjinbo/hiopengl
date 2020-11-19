#version 300 es
layout (location = 0) in vec4 vPosition;

out vec4 vColor;
uniform mat4 vMatrix;

void main() {
     gl_Position  = vMatrix * vPosition;
     vColor = vec4(0.0f, 1.0f, 0.0f, 0.5f);
}