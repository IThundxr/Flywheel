package dev.engine_room.flywheel.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.resources.Identifier;

public final class FlwVertexFormats {
	public static final Identifier LAYOUT_SHADER = IdentifierUtil.id("internal/vertex_input.vert");
	public static final VertexFormat MAIN_FORMAT = VertexFormat.builder(0)
			.addAttribute("_flw_aPos", GpuFormat.RGB32_FLOAT)
			.addAttribute("_flw_aColor", GpuFormat.RGBA8_UNORM)
			.addAttribute("_flw_aTexCoord", GpuFormat.RG32_FLOAT)
			.addAttribute("_flw_aOverlay", GpuFormat.RG16_SINT)
			.addAttribute("_flw_aLight", GpuFormat.RG16_UINT)
			.addAttribute("_flw_aNormal", GpuFormat.RGBA8_SNORM)
			.build();

	private FlwVertexFormats() {
	}

	public static VertexView createVertexView() {
		return new FullVertexView();
	}
}
