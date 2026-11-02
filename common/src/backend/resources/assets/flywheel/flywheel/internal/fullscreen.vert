// This'll be true whenever it's being compiled through a render pipeline
#ifdef VULKAN
#define flw_vertexIndex gl_VertexIndex
#else
#define flw_vertexIndex gl_VertexID
#endif

void main() {
    vec2 vertices[3] = vec2[3](vec2(-1, -1), vec2(3, -1), vec2(-1, 3));
    gl_Position = vec4(vertices[flw_vertexIndex], 0, 1);
}
