package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;

import dev.engine_room.flywheel.backend.engine.instancing.InstancedInstancer;
import dev.engine_room.flywheel.backend.engine.instancing.InstancedLight;

// TODO b3d-ification: Are the names here actually used in shaders or can they be anything?
public class FlwBindGroupLayouts {
	public static final BindGroupLayout FLYWHEEL_BIND_GROUP = BindGroupLayout.builder()
			.withSampler("Sampler0")
			.withSampler("Sampler1")
			.withSampler("Sampler2")
			.withUniform(InstancedLight.LUT_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.withUniform(InstancedLight.SECTIONS_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.withUniform(InstancedInstancer.TEXEL_BUFFER_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.build();
}
