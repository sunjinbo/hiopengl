#version 300 es

precision mediump float;

in vec2 vTexCoord;

out vec4 fragColor;

uniform sampler2D uTexture;
uniform int uRGB2YUV;
uniform float uOffset;

//RGB to YUV
//Y =  0.299R + 0.587G + 0.114B
//U = -0.147R - 0.289G + 0.436B
//V =  0.615R - 0.515G - 0.100B

const vec3 COEF_Y = vec3( 0.299,  0.587,  0.114);
const vec3 COEF_U = vec3(-0.147, -0.289,  0.436);
const vec3 COEF_V = vec3( 0.615, -0.515, -0.100);

void main() {
    if (uRGB2YUV == 1)
    {
        vec2 texelOffset = vec2(uOffset, 0.0);
        vec4 color0 = texture(uTexture, vTexCoord);
        vec4 color1 = texture(uTexture, vTexCoord + texelOffset); // 偏移 offset 采样

        float y0 = dot(color0.rgb, COEF_Y);
        float u0 = dot(color0.rgb, COEF_U) + 0.5;
        float v0 = dot(color0.rgb, COEF_V) + 0.5;
        float y1 = dot(color1.rgb, COEF_Y);

        fragColor = vec4(y0, u0, y1, v0);
    }
    else
    {
        fragColor = texture(uTexture, vTexCoord);
    }
}
