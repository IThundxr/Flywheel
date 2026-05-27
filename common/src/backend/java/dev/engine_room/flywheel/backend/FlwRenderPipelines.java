package dev.engine_room.flywheel.backend;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

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

// TODO b3d-ification: This right now only supports the instancing backend, we need some way for backends to be able
// to provide extra bind group layouts that should be used
public class FlwRenderPipelines {
	private static final HashMap<CacheKey, RenderPipeline.Snippet> SNIPPET_CACHE = new HashMap<>();

	private static final RenderPipeline.Snippet BASE_SNIPPET = RenderPipeline.builder()
			.withVertexBinding(0, FlwVertexFormats.MAIN_FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withBindGroupLayout(FlwBindGroupLayouts.BASE_SAMPLERS)
			.withBindGroupLayout(FlwBindGroupLayouts.UNIFORMS)
			.buildSnippet();

	public static final RenderPipeline.Snippet INSTANCING_SNIPPET = RenderPipeline.builder()
			.withBindGroupLayout(FlwBindGroupLayouts.INSTANCING_TEXEL_BUFFER)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_DEPTH_RANGE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendOp.MAX)))
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_TRANSMITTANCE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE)))
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_DEPTH_RANGE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_BLUE_NOISE)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_DEPTH_TRANSMITTANCE = RenderPipeline.builder()
			.withVertexBinding(0, FlwVertexFormats.EMPTY_FORMAT)
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
			.withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_NONE))
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_DEPTH_RANGE)
			.withBindGroupLayout(FlwBindGroupLayouts.OIT_COEFFICIENTS)
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_ACCUMULATE = RenderPipeline.builder()
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE, BlendFactor.ONE)))
			.buildSnippet();

	public static final RenderPipeline.Snippet OIT_COMPOSITE = RenderPipeline.builder()
			.withVertexBinding(0, FlwVertexFormats.EMPTY_FORMAT)
			// The composite shader writes out the closest depth to gl_FragDepth.
			// depthMask = true: OIT stuff renders on top of other transparent stuff.
			// depthMask = false: other transparent stuff renders on top of OIT stuff.
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
	public static RenderPipeline.Snippet getSnippet(Material material, ContextShader contextShader, RenderPipeline.@Nullable Snippet snippet) {
		return SNIPPET_CACHE.computeIfAbsent(new CacheKey(material, contextShader, snippet), key -> {
			Material m = key.material;
			Snippet extraSnippet = key.snippet;

			Snippet[] snippets = new Snippet[] { BASE_SNIPPET };
			if (extraSnippet != null) {
				snippets = ArrayUtils.add(snippets, extraSnippet);
			}

			RenderPipeline.Builder builder = RenderPipeline.builder(snippets);
			key.contextShader.onBuildPipeline(builder);

			builder.withCull(m.backfaceCulling());
			if (m.depthStencilState() != null)
				builder.withDepthStencilState(m.depthStencilState());
			if (m.colorTargetState() != null)
				builder.withColorTargetState(m.colorTargetState());

			return builder.buildSnippet();
		});
	}

	private record CacheKey(Material material, ContextShader contextShader, RenderPipeline.@Nullable Snippet snippet) {
	}

	@Internal
	public static void clearCache() {
		SNIPPET_CACHE.clear();
	}
}
