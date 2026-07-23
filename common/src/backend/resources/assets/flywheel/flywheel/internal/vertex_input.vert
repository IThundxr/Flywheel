in vec3 _flw_aPos;
in vec4 _flw_aColor;
in vec2 _flw_aTexCoord;
#ifdef FLW_OPENGL
in vec2 _flw_aOverlay;
#else
in ivec2 _flw_aOverlay;
#endif
in uvec2 _flw_aLight;
in vec4 _flw_aNormal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_aPos, 1.0);
    flw_vertexColor = _flw_aColor;
    flw_vertexTexCoord = _flw_aTexCoord;
    #ifdef FLW_OPENGL
    // Integer vertex attributes explode on some drivers for some draw calls, so get the driver
    // to cast the int to a float so we can cast it back to an int and reliably get a sane value.
    flw_vertexOverlay = ivec2(_flw_aOverlay);
    #else
    flw_vertexOverlay = _flw_aOverlay;
    #endif
    flw_vertexLight = _flw_aLight / 256.0;
    flw_vertexNormal = _flw_aNormal.xyz;
}
