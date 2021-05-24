#version 300 es
layout (location = 0) in vec3 position;

out vec3 TexCoords;

uniform mat4 mvp;

void main()
{
    gl_Position = mvp * vec4(position, 1.0);
    TexCoords = position;
}
