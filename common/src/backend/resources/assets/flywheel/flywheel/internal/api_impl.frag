#include "flywheel:internal/material.glsl"
#include "flywheel:internal/api_impl.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"

layout(location = 0) in vec4 flw_vertexPos;
layout(location = 1) in vec4 flw_vertexColor;
layout(location = 2) in vec2 flw_vertexTexCoord;
layout(location = 3) flat in ivec2 flw_vertexOverlay;
layout(location = 4) in vec2 flw_vertexLight;
layout(location = 5) in vec3 flw_vertexNormal;

layout(location = 6) in float flw_sphericalDistance;
layout(location = 7) in float flw_cylindricalDistance;

vec4 flw_sampleColor;

FlwMaterial flw_material;

bool flw_fragDiffuse;
vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;

uniform sampler2D flw_diffuseTex;
uniform sampler2D flw_overlayTex;
uniform sampler2D flw_lightTex;
