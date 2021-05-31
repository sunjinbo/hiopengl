uniform mat4 u_Matrix;

attribute vec4 a_Position;

varying float vLife;

void main()
{
    vLife = a_Position.w;
    gl_Position = vec4(a_Position.xyz, 1.0);
    gl_PointSize = 20.0;
}
