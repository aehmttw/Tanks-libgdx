uniform sampler2D tex;
uniform bool texture;
varying vec4 pos;

void main(void)
{
    mat4 transform = getTransform();
    gl_Position = gl_ModelViewMatrix * getPos(transform);
    gl_Position = gl_ProjectionMatrix * gl_Position;
    pos = gl_Position / gl_Position.w;

    pos.z = ((gl_DepthRange.diff * pos.z) + gl_DepthRange.near + gl_DepthRange.far) / 2.0;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}