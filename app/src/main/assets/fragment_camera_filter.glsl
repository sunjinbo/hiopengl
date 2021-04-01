#version 300 es

precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform samplerExternalOES sTexture

void main() {
     fragColor = texture(sTexture, vTexCoord);
}