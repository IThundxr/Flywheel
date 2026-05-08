package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.pipeline.RenderPipeline;

// TODO - We likely need some cache that can turn materials into render pipelines
public class FlwRenderPipelines {
	private static RenderPipeline.Snippet FLYWHEEL_RENDER_PIPELINE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, InternalVertex.FORMAT)
			.withBindGroupLayout(FlwBindGroupLayouts.INSTANCED_LIGHT)
			.buildSnippet();

	public static RenderPipeline FLYWHEEL_RENDER_PIPELINE = RenderPipeline.builder(FLYWHEEL_RENDER_PIPELINE_SNIPPET)
			.build();
}
