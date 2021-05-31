precision mediump float;

uniform sampler2D u_Texture;

varying float vLife;

void main()
{
    vec4 color = texture2D(u_Texture, gl_PointCoord);
    gl_FragColor = mix(vec4(0.0), color, vLife);
}
