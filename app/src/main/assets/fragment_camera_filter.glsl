#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform samplerExternalOES sTexture;

void main() {
     vec4 color = texture(sTexture, vTexCoord);
     fragColor = vec4(color.r * 0.5, color.g * 0.5, color.b, 1.0);
}