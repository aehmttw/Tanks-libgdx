uniform sampler2D tex;
uniform bool texture;

varying vec4 pos;

void main(void)
{
    float z1 = pos.z * 256.0;
    float z2 = z1 * 256.0;
    float z3 = z2 * 256.0;

    int d1 = int(z1);
    int d2 = int(z2 - float(d1 * 256));
    int d3 = int(z3 - float((d1 * 256 + d2) * 256));

    gl_FragColor = vec4(float(d1) / 256.0, float(d2) / 256.0, float(d2) / 256.0, 1);
    //    if (texture && ((texture2D(tex, gl_TexCoord[0].st)).a <= 0.0))
//        discard;
}