#version 300 es
precision mediump float;

in vec4 vColor;
in vec4 vNormal;

out vec4 fragColor;

void main() {
     float ambientStrength = 0.1f;
     vec3 lightColor = vec3(1.0f, 1.0f, 1.0f); // light color is white
     vec3 ambient = ambientStrength * lightColor;
     vec3 objectColor = vec3(vColor.xyz);
     vec3 result = ambient * objectColor;
     fragColor = vec4(result, 1.0f);
}