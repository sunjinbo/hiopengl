#version 300 es
precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    vec4 color = texture(uTexture, vTexCoord);
    fragColor = vec4(color.rgb, 1.0);
}