attribute vec4 aPosition; // 顶点位置
attribute vec4 aTexCoord; // ST纹理坐标

varying vec2 vTexCoord;

uniform mat4 uMatrix;
uniform mat4 uSTMatrix;

void main() {
    vTexCoord = (uSTMatrix * aTexCoord).xy;
    gl_Position = uMatrix*aPosition;
}
