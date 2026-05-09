package dev.engine_room.flywheel.backend;

import java.util.HashMap;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.api.material.Material;

// TODO - We likely need some cache that can turn materials into render pipelines
public class FlwRenderPipelines {
	private static final HashMap<Material, RenderPipeline> PIPELINE_CACHE = new HashMap<>();

	private static RenderPipeline.Snippet FLYWHEEL_RENDER_PIPELINE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, InternalVertex.FORMAT)
			.withBindGroupLayout(FlwBindGroupLayouts.INSTANCED_LIGHT)
			.buildSnippet();

	public static RenderPipeline FLYWHEEL_RENDER_PIPELINE = RenderPipeline.builder(FLYWHEEL_RENDER_PIPELINE_SNIPPET)
			.build();

	public static RenderPipeline getForMaterial(Material material) {
		return PIPELINE_CACHE.computeIfAbsent(material, m -> {
			return RenderPipeline.builder(FLYWHEEL_RENDER_PIPELINE_SNIPPET)
					.withCull(m.backfaceCulling())
					.withDepthStencilState(m.depthStencilState())
					.build();
		});
	}

	public record PipelineKey() {
		//		setupTransparency(material.transparency()); // Should be handled by RenderPipeline
		//		setupWriteMask(material.writeMask()); // Should be handled by RenderPipeline
	}
}
