#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/instancing/light.glsl"

uniform uvec2 _flw_packedMaterial;

#ifdef GL_ARB_shader_draw_parameters
#define flw_baseInstance gl_BaseInstance
#define flw_baseVertex gl_BaseVertex
#else
uniform int flw_baseInstance = 0;
uniform uint flw_baseVertex;
#endif

#ifdef FLW_EMBEDDED
uniform mat4 _flw_modelMatrixUniform;
uniform mat3 _flw_normalMatrixUniform;
#endif

void main() {
    _flw_unpackMaterialProperties(_flw_packedMaterial.y, flw_material);

    FlwInstance instance = _flw_unpackInstance(flw_baseInstance + gl_InstanceID);

    #ifdef FLW_EMBEDDED
    _flw_modelMatrix = _flw_modelMatrixUniform;
    _flw_normalMatrix = _flw_normalMatrixUniform;
    #endif

    _flw_main(instance, uint(gl_InstanceID), flw_baseVertex);
}
