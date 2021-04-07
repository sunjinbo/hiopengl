#version 300 es
precision mediump float;

uniform sampler2D uTexture;

out vec4 fragColor;

void main() {
     fragColor = texture2D(uTexture, gl_PointCoord);
     fragColor = vec4(1.0, 0.0, 1.0, 1.0);
     //fragColor = texture2D(uTexture, vec2(0.5, 0.5));
}