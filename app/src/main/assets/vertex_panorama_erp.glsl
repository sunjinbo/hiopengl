#version 320 es

in vec3 position;
in vec3 inColor;

uniform mat4 vMatrix;

out vec3 varColor;

void main() {
    gl_Position = vMatrix * vec4(position, 1.0);
    varColor = vec4(inColor, 1.0).xyz;
}
