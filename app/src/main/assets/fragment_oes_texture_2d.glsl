#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform samplerExternalOES uTexture;

void main() {
     fragColor = texture(uTexture, vTexCoord);
}