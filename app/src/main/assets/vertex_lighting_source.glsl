#version 300 es
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec3 aNormal;

out vec3 vColor;
out vec3 vNormal;
out vec3 FragPos;

uniform mat4 vMatrix;
uniform mat4 vModel;

void main() {
     gl_Position  = vMatrix * vec4(vPosition, 1.0f);
     vColor = vec3(1.0f, 0.5f, 0.31f);
     FragPos = vec3(vModel * vec4(vPosition, 1.0f));
     vNormal = mat3(transpose(inverse(vModel))) * aNormal;
}
