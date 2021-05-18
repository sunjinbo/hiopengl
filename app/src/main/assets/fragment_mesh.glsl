#version 300 es
precision mediump float;

in vec3 vBC;

out vec4 fragColor;

void main() {
    if (any(lessThan(vBC, vec3(0.01))))
    {
        fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }
    else
    {
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}