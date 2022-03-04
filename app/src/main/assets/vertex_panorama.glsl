#version 320 es

in vec3 position;
in vec2 inTexcoord;

uniform mat4 vMatrix;

out vec2 varTexcoord;

void main() {
   gl_Position = vMatrix * vec4(position, 1.0);
   varTexcoord = vec4(inTexcoord, 1.0, 1.0).xy;
}