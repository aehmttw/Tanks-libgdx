#version 120

uniform float obstacleSizeFrac;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    return vec4(gl_Vertex.x, gl_Vertex.y, gl_Vertex.z * (1.0 - obstacleSizeFrac), gl_Vertex.w);
}

vec3 getNormal(mat4 transform)
{
    return (gl_ModelViewProjectionMatrix * vec4(gl_Normal, 0)).xyz;
}

void getVertVecs(out vec4 pos, out vec3 normal)
{
    pos = getPos(getTransform());
    normal = getNormal(getTransform());
}

vec4 getColor(vec4 colorIn)
{
    return colorIn * (1.0 - obstacleSizeFrac) + vec4(0.6823, 0.3608, 0.0628, 1.0) * obstacleSizeFrac;
}