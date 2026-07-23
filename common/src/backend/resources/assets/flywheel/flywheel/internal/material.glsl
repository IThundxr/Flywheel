const uint FLW_MAT_CARDINAL_LIGHTING_MODE_OFF = 0u;
const uint FLW_MAT_CARDINAL_LIGHTING_MODE_CHUNK = 1u;
const uint FLW_MAT_CARDINAL_LIGHTING_MODE_ENTITY = 2u;

const uint B3D_COMPARE_OP_ALWAYS_PASS = 0u;
const uint B3D_COMPARE_OP_LESS_THAN = 1u;
const uint B3D_COMPARE_OP_LESS_THAN_OR_EQUAL = 2u;
const uint B3D_COMPARE_OP_EQUAL = 3u;
const uint B3D_COMPARE_OP_NOT_EQUAL = 4u;
const uint B3D_COMPARE_OP_GREATER_THAN_OR_EQUAL = 5u;
const uint B3D_COMPARE_OP_GREATER_THAN = 6u;
const uint B3D_COMPARE_OP_NEVER_PASS = 7u;

const uint B3D_BLEND_FACTOR_CONSTANT_ALPHA = 0u;
const uint B3D_BLEND_FACTOR_CONSTANT_COLOR = 1u;
const uint B3D_BLEND_FACTOR_DST_ALPHA = 2u;
const uint B3D_BLEND_FACTOR_DST_COLOR = 3u;
const uint B3D_BLEND_FACTOR_ONE = 4u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA = 5u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR = 6u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_DST_ALPHA = 7u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_DST_COLOR = 8u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA = 9u;
const uint B3D_BLEND_FACTOR_ONE_MINUS_SRC_COLOR = 10u;
const uint B3D_BLEND_FACTOR_SRC_ALPHA = 11u;
const uint B3D_BLEND_FACTOR_SRC_ALPHA_SATURATE = 12u;
const uint B3D_BLEND_FACTOR_SRC_COLOR = 13u;
const uint B3D_BLEND_FACTOR_ZERO = 14u;

const uint B3D_BLEND_OP_ADD = 0u;
const uint B3D_BLEND_OP_SUBTRACT = 1u;
const uint B3D_BLEND_OP_REVERSE_SUBTRACT = 2u;
const uint B3D_BLEND_OP_MIN = 3u;
const uint B3D_BLEND_OP_MAX = 4u;

const int B3D_COLOR_TARGET_STATE_WRITE_RED   = 1;
const int B3D_COLOR_TARGET_STATE_WRITE_GREEN = 2;
const int B3D_COLOR_TARGET_STATE_WRITE_BLUE  = 4;
const int B3D_COLOR_TARGET_STATE_WRITE_ALPHA = 8;

const int B3D_COLOR_TARGET_STATE_WRITE_COLOR = 7;
const int B3D_COLOR_TARGET_STATE_WRITE_ALL   = 15;
const int B3D_COLOR_TARGET_STATE_WRITE_NONE  = 0;

struct BlendEquation {
    uint sourceFactor; // B3D_BLEND_FACTOR_*
    uint destFactor; // B3D_BLEND_FACTOR_*
    uint blendOp; // B3D_BLEND_OP_*
};

struct BlendFunction {
    BlendEquation color;
    BlendEquation alpha;
};

struct ColorTargetState {
    bool hasBlendFunction;
    BlendFunction blendFunction;

    uint format;
    uint writeMask; // B3D_COLOR_TARGET_STATE_WRITE_* Bitmask flags
};

struct DepthStencilState {
    uint compareOp; // B3D_COMPARE_OP_*
    bool writeDepth;
    float depthBiasScaleFactor;
    float depthBiasConstant;
};

struct FlwMaterial {
    bool blur;
    bool mipmap;
    bool backfaceCulling;
    // TODO B3D-ification: Reintroduce these
//    DepthStencilState depthStencilState;
//    ColorTargetState colorTargetState;
    bool useOverlay;
    bool useOit;
    bool useLight;
    uint cardinalLightingMode;
    bool ambientOcclusion;
};
