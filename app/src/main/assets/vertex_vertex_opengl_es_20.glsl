attribute vec4 aPosition; // 顶点位置
attribute vec4 aColor; // 顶点颜色

varying vec4 vColor;

uniform mat4 uMatrix; // ViewModel矩阵

void main() {
    gl_Position  = uMatrix * aPosition;
    vColor = aColor;
}
