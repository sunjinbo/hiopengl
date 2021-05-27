#version 300 es
precision mediump float;

uniform samplerCube cubemaps;

in vec3 TexCoords;

out vec4 fragColor;

void main() {
    fragColor = texture(cubemaps, TexCoords);
}