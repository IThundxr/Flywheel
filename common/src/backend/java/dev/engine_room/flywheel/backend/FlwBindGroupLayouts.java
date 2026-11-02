package dev.engine_room.flywheel.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.UniformType;

import dev.engine_room.flywheel.backend.engine.instancing.InstancedInstancer;
import dev.engine_room.flywheel.backend.engine.instancing.InstancedLight;
import dev.engine_room.flywheel.backend.engine.uniform.FlwUniform;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import net.minecraft.util.Util;

public class FlwBindGroupLayouts {
	public static final BindGroupLayout BASE_SAMPLERS = BindGroupLayout.builder()
			.withUniform("flw_diffuseTex", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("flw_overlayTex", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("flw_lightTex", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout CRUMBLING_SAMPLER = BindGroupLayout.builder()
			.withUniform("_flw_crumblingTex", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout OIT_ACCUMULATE = BindGroupLayout.builder()
			.withUniform("_flw_accumulate", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout OIT_DEPTH_RANGE = BindGroupLayout.builder()
			.withUniform("_flw_depthRange", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout OIT_BLUE_NOISE = BindGroupLayout.builder()
			.withUniform("_flw_blueNoise", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout OIT_COEFFICIENTS = BindGroupLayout.builder()
			.withUniform("_flw_coefficients0", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("_flw_coefficients1", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("_flw_coefficients2", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("_flw_coefficients3", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

	public static final BindGroupLayout INSTANCING_TEXEL_BUFFERS = BindGroupLayout.builder()
			.withUniform(InstancedInstancer.TEXEL_BUFFER_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_UINT)
			// TODO - Technically, lighting should be split but doing so errors on vulkan at the moment
			.withUniform(InstancedLight.LUT_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.withUniform(InstancedLight.SECTIONS_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.build();

	public static final BindGroupLayout UNIFORMS = Util.make(() -> {
		BindGroupLayout.Builder builder = BindGroupLayout.builder();
		for (FlwUniform uniform : Uniforms.UNIFORMS)
			builder.withUniform(uniform.getUniformName(), UniformType.UNIFORM_BUFFER);
		return builder.build();
	});
}
