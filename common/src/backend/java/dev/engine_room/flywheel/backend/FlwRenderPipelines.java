package dev.engine_room.flywheel.backend;

import java.util.HashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.compile.ContextShader;

// TODO b3d-ification: This right now only supports the instancing backend, we need some way for backends to be able
// to provide extra bind group layouts that should be used
public class FlwRenderPipelines {
	private static final HashMap<CacheKey, RenderPipeline.Snippet> SNIPPET_CACHE = new HashMap<>();

	private static final RenderPipeline.Snippet BASE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, InternalVertex.FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withBindGroupLayout(FlwBindGroupLayouts.BASE_SAMPLERS)
			.withBindGroupLayout(FlwBindGroupLayouts.UNIFORMS)
			.buildSnippet();

	public static final RenderPipeline.Snippet INSTANCING_SNIPPET = RenderPipeline.builder()
			.withBindGroupLayout(FlwBindGroupLayouts.INSTANCING_TEXEL_BUFFER)
			.buildSnippet();

	@SuppressWarnings("DataFlowIssue")
	public static RenderPipeline.Snippet getSnippet(Material material, ContextShader contextShader) {
		return SNIPPET_CACHE.computeIfAbsent(new CacheKey(material, contextShader), key -> {
			Material m = key.material;

			RenderPipeline.Builder builder = RenderPipeline.builder(BASE_SNIPPET);
			key.contextShader.onBuildPipeline(builder);

			builder.withCull(m.backfaceCulling());
			if (m.depthStencilState() != null)
				builder.withDepthStencilState(m.depthStencilState());
			if (m.colorTargetState() != null)
				builder.withColorTargetState(m.colorTargetState());

			return builder.buildSnippet();
		});
	}

	private record CacheKey(Material material, ContextShader contextShader) {
	}

	@Internal
	public static void clearCache() {
		SNIPPET_CACHE.clear();
	}
}
