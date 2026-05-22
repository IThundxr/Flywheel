package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;

import dev.engine_room.flywheel.backend.engine.instancing.InstancedInstancer;
import dev.engine_room.flywheel.backend.engine.instancing.InstancedLight;
import dev.engine_room.flywheel.backend.engine.uniform.FlwUniform;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import net.minecraft.util.Util;

// TODO b3d-ification: Support for other samplers for things like OIT
public class FlwBindGroupLayouts {
	public static final BindGroupLayout BASE_SAMPLERS = BindGroupLayout.builder()
			.withSampler("flw_diffuseTex")
			.withSampler("flw_overlayTex")
			.withSampler("flw_lightTex")
			.build();

	public static final BindGroupLayout CRUMBLING_SAMPLER = BindGroupLayout.builder()
			.withSampler("_flw_crumblingTex")
			.build();

	public static final BindGroupLayout OIT_SAMPLERS = BindGroupLayout.builder()
			.withSampler("_flw_accumulate")
			.withSampler("_flw_depthRange")
			.withSampler("_flw_coefficients")
			.build();

	public static final BindGroupLayout INSTANCING_TEXEL_BUFFER = BindGroupLayout.builder()
			.withUniform(InstancedInstancer.TEXEL_BUFFER_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_UINT)
			.build();

	public static final BindGroupLayout LIGHT_TEXEL_BUFFERS = BindGroupLayout.builder()
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
