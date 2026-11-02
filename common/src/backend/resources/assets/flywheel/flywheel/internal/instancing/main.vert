#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/instancing/light.glsl"

#ifdef RENDERPEARL_INSTANCE_INDEX_INCLUDES_BASE_INSTANCE
#define flw_baseInstance gl_BaseInstanceARB
#define flw_baseVertex gl_BaseVertexARB
#endif

#ifdef _FLW_VULKAN
#define flw_globalInstance gl_InstanceIndex
#define flw_localInstance  (gl_InstanceIndex - flw_baseInstance)
#else
#define flw_globalInstance (flw_baseInstance + gl_InstanceIndex)
#define flw_localInstance  gl_InstanceIndex
#endif

layout(push_constant) uniform FlwPushConstants {
    uvec2 _flw_packedMaterial;
    #ifndef RENDERPEARL_INSTANCE_INDEX_INCLUDES_BASE_INSTANCE
    int flw_baseInstance;
    uint flw_baseVertex;
    #endif
    #ifdef FLW_EMBEDDED
    mat4 _flw_modelMatrixUniform;
    mat3 _flw_normalMatrixUniform;
    #endif
};

void main() {
    _flw_unpackMaterialProperties(_flw_packedMaterial.y, flw_material);

    FlwInstance instance = _flw_unpackInstance(flw_globalInstance);

    #ifdef FLW_EMBEDDED
    _flw_modelMatrix = _flw_modelMatrixUniform;
    _flw_normalMatrix = _flw_normalMatrixUniform;
    #endif

    _flw_main(instance, flw_localInstance, flw_baseVertex);
}
