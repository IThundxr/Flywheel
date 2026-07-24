#include "flywheel:internal/common.frag"
#include "flywheel:internal/instancing/light.glsl"

#ifdef FLW_VULKAN
layout(push_constant) uniform FlwPushConstants {
    uvec2 _flw_packedMaterial;
};
#else
uniform uvec2 _flw_packedMaterial;
#endif

void main() {
    _flw_unpackUint2x16(_flw_packedMaterial.x, _flw_uberFogIndex, _flw_uberCutoutIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.y, flw_material);

    _flw_main();
}
