#include "flywheel:internal/wavelet.glsl"
#include "flywheel:internal/depth.glsl"
#include "flywheel:internal/uniforms/frame.glsl"

out vec4 frag;

uniform sampler2D _flw_accumulate;
uniform sampler2D _flw_depthRange;

uniform sampler2D _flw_coefficients0;
uniform sampler2D _flw_coefficients1;
uniform sampler2D _flw_coefficients2;
uniform sampler2D _flw_coefficients3;

void main() {
    vec4 texel = texelFetch(_flw_accumulate, ivec2(gl_FragCoord.xy), 0);

    if (texel.a < 1e-5) {
        discard;
    }

    sampler2D[4] _flw_coefficients;
    _flw_coefficients[0] = _flw_coefficients0;
    _flw_coefficients[1] = _flw_coefficients1;
    _flw_coefficients[2] = _flw_coefficients2;
    _flw_coefficients[3] = _flw_coefficients3;

    float total_transmittance = total_transmittance(_flw_coefficients);

    frag = vec4(texel.rgb / texel.a, 1. - total_transmittance);

    float minDepth = -texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0).r;

    gl_FragDepth = delinearize_depth(minDepth, _flw_cullData.znear, _flw_cullData.zfar);
}
