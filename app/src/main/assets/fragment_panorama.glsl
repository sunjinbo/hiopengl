#version 320 es
precision mediump float;
in lowp vec2 varTexcoord;

uniform sampler2D text_yuv;
out lowp vec4 FragColor;

void main (void)
{
    FragColor = texture(text_yuv, varTexcoord);
//    FragColor = vec4(1.0, 0.0, 1.0, 1.0);
}
