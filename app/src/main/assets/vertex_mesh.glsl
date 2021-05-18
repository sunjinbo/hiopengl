#version 300 es
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec3 vBary;

uniform mat4 vMatrix;

out vec3 vBC;

void main() {
     vBC = vBary;
     gl_Position  = vMatrix * vec4(vPosition, 1.0);
}
