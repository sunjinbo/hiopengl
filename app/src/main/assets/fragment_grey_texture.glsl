#version 300 es
precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    vec4 pixelColor = texture(uTexture, vTexCoord);
    float luminance = pixelColor.r * 0.299 + pixelColor.g * 0.587 + pixelColor.b * 0.114;
    fragColor = vec4(luminance, luminance, luminance, 1.0);
}