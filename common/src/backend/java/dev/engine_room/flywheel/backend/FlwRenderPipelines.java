package dev.engine_room.flywheel.backend;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.BlendOp;
import com.mojang.blaze3d.platform.CompareOp;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.PipelineCompiler.OitMode;

public class FlwRenderPipelines {
	private static final HashMap<CacheKey, RenderPipeline.Snippet> SNIPPET_CACHE = new HashMap<>();

	private static final RenderPipeline.Snippet BASE_VERTEX_BINDING = RenderPipeline.builder()
			.withVertexBinding(0, FlwVertexFormats.MAIN_FORMAT)
			.buildSnippet();

	private static final RenderPipeline.Snippet BASE_TOPOLOGY_AND_BIND_GROUPS = RenderPipeline.builder()
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withBindGroupLayout(FlwBindGroupLayouts.BASE_SAMPLERS)
			.withBindGroupLayout(FlwBindGroupLayouts.UNIFORMS)
			.withBindGroupLayout(FlwBindGroupLayouts.LIGHT_TEXEL_BUFFERS)
			.buildSnippet();

	public static final RenderPipeline.Snippet INSTANCING_SNIPPET = RenderPipeline.builder()
			.withBindGroupLayout(FlwBindGroupLayouts.INSTANCING_TEXEL_BUFFER)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_DEPTH_RANGE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(new ColorTargetState(Optional.of(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendOp.MAX)), GpuFormat.RG32_FLOAT, ColorTargetState.WRITE_ALL))
			.buildSnippet();

	private static final ColorTargetState TRANSMITTANCE_STATE = new ColorTargetState(
			Optional.of(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE)),
			GpuFormat.RGBA16_FLOAT,
			ColorTargetState.WRITE_ALL
	);
	public static final RenderPipeline.Snippet OIT_TRANSMITTANCE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(0, TRANSMITTANCE_STATE)
			.withColorTargetState(1, TRANSMITTANCE_STATE)
			.withColorTargetState(2, TRANSMITTANCE_STATE)
			.withColorTargetState(3, TRANSMITTANCE_STATE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_DEPTH_RANGE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_BLUE_NOISE)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_DEPTH_FROM_TRANSMITTANCE = RenderPipeline.builder(BASE_TOPOLOGY_AND_BIND_GROUPS)
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_NONE))
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_DEPTH_RANGE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_COEFFICIENTS)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_ACCUMULATE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(new ColorTargetState(Optional.of(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE)), GpuFormat.RGBA16_FLOAT, ColorTargetState.WRITE_ALL))
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_COMPOSITE = RenderPipeline.builder(BASE_TOPOLOGY_AND_BIND_GROUPS)
			// The composite shader writes out the closest depth to gl_FragDepth.
			// depthWrite = true: OIT stuff renders on top of other transparent stuff.
			// depthWrite = false: other transparent stuff renders on top of OIT stuff.
			// If Neo gets wavelet OIT we can use their hooks to be correct with everything.
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
			// We rely on the blend func to achieve:
			// final color = (1 - transmittance_total) * sum(color_f * alpha_f * transmittance_f) / sum(alpha_f * transmittance_f)
			//			+ color_dst * transmittance_total
			//
			// Though note that the alpha value we emit in the fragment shader is actually (1. - transmittance_total).
			// The extra inversion step is so we can have a sane alpha value written out for the fabulous blit shader to consume.
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendOp.ADD, BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendOp.ADD)))
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_ACCUMULATE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_DEPTH_RANGE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_COEFFICIENTS)
			.buildSnippet();

	@SuppressWarnings("DataFlowIssue")
	public static RenderPipeline.Snippet getSnippet(Material material, ContextShader contextShader, OitMode oit) {
		return SNIPPET_CACHE.computeIfAbsent(new CacheKey(material, contextShader, oit), key -> {
			Material m = key.material;
			OitMode oitMode = key.oit;

			Snippet[] snippets = new Snippet[] { BASE_VERTEX_BINDING, BASE_TOPOLOGY_AND_BIND_GROUPS };
			if (oitMode.snippet != null) {
				snippets = ArrayUtils.add(snippets, oitMode.snippet);
			}

			RenderPipeline.Builder builder = RenderPipeline.builder(snippets);
			key.contextShader.onBuildPipeline(builder);

			builder.withCull(m.backfaceCulling());

			if (m.depthStencilState() != null) {
				builder.withDepthStencilState(m.depthStencilState());
			}

			if (m.colorTargetState() != null) {
				// TODO b3d-ification: Not sure if this is correct
				if (oitMode == OitMode.OFF) {
					builder.withColorTargetState(m.colorTargetState());
				} else {
					ColorTargetState materialColorTargetState = m.colorTargetState();
					ColorTargetState oitColorTargetState = oitMode.snippet.colorTargetStates()[0];

					ColorTargetState newState = new ColorTargetState(
							oitColorTargetState.blendFunction(),
							oitColorTargetState.format(),
							materialColorTargetState.writeMask()
					);
					builder.withColorTargetState(newState);
				}
			}

			return builder.buildSnippet();
		});
	}

	private record CacheKey(Material material, ContextShader contextShader, OitMode oit) {
	}

	@Internal
	public static void clearCache() {
		SNIPPET_CACHE.clear();
	}
}
