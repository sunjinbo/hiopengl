#version 320 es
precision mediump float;
in lowp vec3 varColor;

out lowp vec4 FragColor;

void main (void)
{
    FragColor = vec4(varColor, 1);
    //    FragColor = vec4(1.0, 0.0, 1.0, 1.0);
}
