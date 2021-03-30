#version 300 es
precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
     fragColor = texture(uTexture, vTexCoord);
}