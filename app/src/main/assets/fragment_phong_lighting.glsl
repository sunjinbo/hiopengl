#version 300 es
precision mediump float;

in vec3 vColor;
in vec3 vNormal;
in vec3 FragPos;

out vec4 fragColor;

uniform vec3 viewPos;

void main() {
    // Light
    vec3 lightPos = vec3(1.2f, 1.0f, 2.0f);
    vec3 lightColor = vec3(1.0f, 1.0f, 1.0f);

    // Ambient
    float ambientStrength = 0.1f;
    vec3 ambient = ambientStrength * lightColor;

    // Diffuse
    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = diff * lightColor;

    // Specular
    float specularStrength = 0.5f;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0f), 32.0f);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * vColor;

    fragColor = vec4(result, 1.0f);
}
