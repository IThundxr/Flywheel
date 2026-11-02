#include "flywheel:internal/material.glsl"
#include "flywheel:internal/api_impl.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"

layout(location = 0) out vec4 flw_vertexPos;
layout(location = 1) out vec4 flw_vertexColor;
layout(location = 2) out vec2 flw_vertexTexCoord;
layout(location = 3) flat out ivec2 flw_vertexOverlay;
layout(location = 4) out vec2 flw_vertexLight;
layout(location = 5) out vec3 flw_vertexNormal;

layout(location = 6) out float flw_sphericalDistance;
layout(location = 7) out float flw_cylindricalDistance;

FlwMaterial flw_material;

uint flw_vertexId;
