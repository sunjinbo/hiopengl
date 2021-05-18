#version 300 es
#extension GL_OES_standard_derivatives : enable
precision mediump float;

in vec3 vBC;

out vec4 fragColor;

float edgeFactor(){
    vec3 d = fwidth(vBC);
    vec3 a3 = smoothstep(vec3(0.0), d * 1.5, vBC);
    return min(min(a3.x, a3.y), a3.z);
}

void main() {
    fragColor.rgb = mix(vec3(0.0, 0.0, 0.0), vec3(1.0, 1.0, 1.0), edgeFactor());
    fragColor.a = 1.0;
}