package dev.engine_room.flywheel.backend;

import java.util.HashMap;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

// TODO - We likely need some cache that can turn materials into render pipelines
public class FlwRenderPipelines {
	private static final HashMap<Material, RenderPipeline> PIPELINE_CACHE = new HashMap<>();

	private static RenderPipeline.Snippet FLYWHEEL_RENDER_PIPELINE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, InternalVertex.FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withBindGroupLayout(FlwBindGroupLayouts.FLYWHEEL_BIND_GROUP)
			.withVertexShader("core/block")
			.withFragmentShader("core/block")
			.buildSnippet();

	public static RenderPipeline getForMaterial(Material material) {
		return PIPELINE_CACHE.computeIfAbsent(material, m -> {
			RenderPipeline.Builder builder = RenderPipeline.builder(FLYWHEEL_RENDER_PIPELINE_SNIPPET)
				.withLocation(IdentifierUtil.id("flw_" + material.hashCode())); // TODO - Proper location/id

			builder.withCull(m.backfaceCulling());
			builder.withDepthStencilState(m.depthStencilState());
			m.colorTargetState().ifPresent(builder::withColorTargetState);

			return builder.build();
		});
	}

	public record PipelineKey() {
		//		setupTransparency(material.transparency()); // Should be handled by RenderPipeline
		//		setupWriteMask(material.writeMask()); // Should be handled by RenderPipeline
	}
}
