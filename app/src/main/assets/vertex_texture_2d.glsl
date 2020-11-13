#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

out vec4 vColor;
out vec2 vTexCoord;

void main() {
     gl_Position  = vPosition;
     gl_PointSize = 100.0;
     vColor = aColor;
     vTexCoord = aTexCoord;
}