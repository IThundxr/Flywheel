uniform sampler2D _flw_coefficients0;
uniform sampler2D _flw_coefficients1;
uniform sampler2D _flw_coefficients2;
uniform sampler2D _flw_coefficients3;

// TODO: maybe we could reduce the number of texel fetches below?
float get_coefficients(int index) {
    ivec2 coord = ivec2(gl_FragCoord.xy);

    switch (index >> 2) {
        case 0: return texelFetch(_flw_coefficients0, coord, 0)[index & 3];
        case 1: return texelFetch(_flw_coefficients1, coord, 0)[index & 3];
        case 2: return texelFetch(_flw_coefficients2, coord, 0)[index & 3];
        default: return texelFetch(_flw_coefficients3, coord, 0)[index & 3];
    }
}
