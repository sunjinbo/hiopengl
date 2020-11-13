#version 300 es
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 vMatrix;

out vec2 vTexCoord;

void main() {
     vTexCoord = aTexCoord;
     gl_Position  = vMatrix * vec4(vPosition, 1.0f);
}
