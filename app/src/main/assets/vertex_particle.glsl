uniform mat4 u_Matrix;

attribute vec4 a_Position;

varying vec4 vColor;

void main()
{
    vColor = vec4(a_Position.w, a_Position.w, a_Position.w, a_Position.w);
    gl_Position = vec4(a_Position.xyz, 1.0);
    gl_PointSize = 5.0;
}
