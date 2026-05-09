package dev.engine_room.flywheel.backend;

import java.util.HashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.compile.ContextShader;

// TODO - We likely need some cache that can turn materials into render pipelines
public class FlwRenderPipelines {
	// TODO - Support clearing this on resource reload
	private static final HashMap<FlwSnippetKey, RenderPipeline.Snippet> SNIPPET_CACHE = new HashMap<>();

	private static RenderPipeline.Snippet FLYWHEEL_RENDER_PIPELINE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, InternalVertex.FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withBindGroupLayout(FlwBindGroupLayouts.BIND_GROUP)
			.withBindGroupLayout(FlwBindGroupLayouts.BASE_SAMPLERS)
			.withBindGroupLayout(FlwBindGroupLayouts.UNIFORMS)
			.buildSnippet();

	@SuppressWarnings("DataFlowIssue")
	public static RenderPipeline.Snippet getSnippet(Material material, ContextShader contextShader) {
		return SNIPPET_CACHE.computeIfAbsent(new FlwSnippetKey(material, contextShader), key -> {
			Material m = key.material;
			ContextShader cs = key.contextShader;

			RenderPipeline.Builder builder = RenderPipeline.builder(FLYWHEEL_RENDER_PIPELINE_SNIPPET);

			cs.onBuildPipeline(builder);

			builder.withCull(m.backfaceCulling());
			if (m.depthStencilState() != null)
				builder.withDepthStencilState(m.depthStencilState());
			if (m.colorTargetState() != null)
				builder.withColorTargetState(m.colorTargetState());

			return builder.buildSnippet();
		});
	}

	private record FlwSnippetKey(Material material, ContextShader contextShader) {
	}

	@Internal
	public static void clearCache() {
		SNIPPET_CACHE.clear();
	}
}
