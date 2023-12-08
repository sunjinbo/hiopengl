#version 320 es
precision mediump float;
in lowp vec2 varTexcoord;

uniform sampler2D text_yuv;
out lowp vec4 FragColor;

void main (void)
{
    FragColor = texture(text_yuv, vec2(1.0 - varTexcoord.x, 1.0 - varTexcoord.y));
//    FragColor = vec4(1.0, 0.0, 1.0, 1.0);
}
