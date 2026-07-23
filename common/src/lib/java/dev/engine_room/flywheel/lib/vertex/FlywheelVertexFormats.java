package dev.engine_room.flywheel.lib.vertex;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

public class FlywheelVertexFormats {
	/// Basically the same as {@link DefaultVertexFormat#BLOCK} but with the normals included
	public static final VertexFormat BLOCK_VERTEX_FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV2", GpuFormat.RG16_SINT)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.build();
}
