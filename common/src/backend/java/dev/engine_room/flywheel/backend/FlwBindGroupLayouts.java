package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;

import dev.engine_room.flywheel.backend.engine.instancing.InstancedLight;

// TODO b3d-ification: Are the names here actually used in shaders or can they be anything?
public class FlwBindGroupLayouts {
	public static final BindGroupLayout INSTANCED_LIGHT = BindGroupLayout.builder()
			.withUniform(InstancedLight.LUT_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.withUniform(InstancedLight.SECTIONS_BINDING, UniformType.TEXEL_BUFFER, GpuFormat.R32_UINT)
			.build();
}
