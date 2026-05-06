package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.resources.Identifier;

public final class InternalVertex {
	public static final VertexFormat FORMAT = VertexFormat.builder(0)
			.addAttribute("Position", GpuFormat.RGB32_FLOAT)
			.addAttribute("Color", GpuFormat.RGBA8_UNORM)
			.addAttribute("UV0", GpuFormat.RG32_FLOAT)
			.addAttribute("UV1", GpuFormat.RG16_SINT)
			.addAttribute("UV2", GpuFormat.RG16_UINT)
			.addAttribute("Normal", GpuFormat.RGBA8_SNORM)
			.build();

	public static final Identifier LAYOUT_SHADER = IdentifierUtil.id("internal/vertex_input.vert");

	private InternalVertex() {
	}

	public static VertexView createVertexView() {
		return new FullVertexView();
	}
}
