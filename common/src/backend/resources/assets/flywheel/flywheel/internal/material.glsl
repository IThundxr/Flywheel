const uint FLW_MAT_DEPTH_TEST_OFF = 0u;
const uint FLW_MAT_DEPTH_TEST_NEVER = 1u;
const uint FLW_MAT_DEPTH_TEST_LESS = 2u;
const uint FLW_MAT_DEPTH_TEST_EQUAL = 3u;
const uint FLW_MAT_DEPTH_TEST_LEQUAL = 4u;
const uint FLW_MAT_DEPTH_TEST_GREATER = 5u;
const uint FLW_MAT_DEPTH_TEST_NOTEQUAL = 6u;
const uint FLW_MAT_DEPTH_TEST_GEQUAL = 7u;
const uint FLW_MAT_DEPTH_TEST_ALWAYS = 8u;

const uint FLW_MAT_TRANSPARENCY_OPAQUE = 0u;
const uint FLW_MAT_TRANSPARENCY_ADDITIVE = 1u;
const uint FLW_MAT_TRANSPARENCY_LIGHTNING = 2u;
const uint FLW_MAT_TRANSPARENCY_GLINT = 3u;
const uint FLW_MAT_TRANSPARENCY_CRUMBLING = 4u;
const uint FLW_MAT_TRANSPARENCY_TRANSLUCENT = 5u;

const uint FLW_MAT_WRITE_MASK_COLOR_DEPTH = 0u;
const uint FLW_MAT_WRITE_MASK_COLOR = 1u;
const uint FLW_MAT_WRITE_MASK_DEPTH = 2u;

const uint FLW_MAT_CARDINAL_LIGHTING_MODE_OFF = 0u;
const uint FLW_MAT_CARDINAL_LIGHTING_MODE_CHUNK = 1u;
const uint FLW_MAT_CARDINAL_LIGHTING_MODE_ENTITY = 2u;

struct FlwMaterial {
    bool blur;
    bool mipmap;
    bool backfaceCulling;
    bool polygonOffset;
    uint depthTest;
    uint transparency;
    uint writeMask;
    bool useOverlay;
    bool useLight;
    uint cardinalLightingMode;
    bool ambientOcclusion;
};

struct DepthStencilState {
    uint compareOp;
    bool writeDepth;
    float depthBiasScaleFactor;
    float depthBiasConstant;
};

// TODO - Implement this
struct ColorTargetState {

};

struct BlendFunction {
    uint color;
    uint alpha;
};

const uint FLW_BLEND_FACTOR_CONSTANT_ALPHA = 0u;
const uint FLW_BLEND_FACTOR_CONSTANT_COLOR = 1u;
const uint FLW_BLEND_FACTOR_DST_ALPHA = 2u;
const uint FLW_BLEND_FACTOR_DST_COLOR = 3u;
const uint FLW_BLEND_FACTOR_ONE = 4u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA = 5u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR = 6u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_DST_ALPHA = 7u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_DST_COLOR = 8u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA = 9u;
const uint FLW_BLEND_FACTOR_ONE_MINUS_SRC_COLOR = 10u;
const uint FLW_BLEND_FACTOR_SRC_ALPHA = 11u;
const uint FLW_BLEND_FACTOR_SRC_ALPHA_SATURATE = 12u;
const uint FLW_BLEND_FACTOR_SRC_COLOR = 13u;
const uint FLW_BLEND_FACTOR_ZERO = 14u;

const uint FLW_BLEND_OP_ADD = 0u;
const uint FLW_BLEND_OP_SUBTRACT = 1u;
const uint FLW_BLEND_OP_REVERSE_SUBTRACT = 2u;
const uint FLW_BLEND_OP_MIN = 3u;
const uint FLW_BLEND_OP_MAX = 4u;

struct BlendEquation {
    uint sourceFactor;
    uint destFactor;
    uint blendOp;
};
