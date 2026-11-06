package dev.engine_room.flywheel.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPassLayout;

public class FlwRenderPassLayouts {
	public static final RenderPassLayout OIT_DEPTH_RANGE = RenderPassLayout.simple("Flw OIT Depth Range", GpuFormat.RG32_FLOAT, GpuFormat.D32_FLOAT);
	public static final RenderPassLayout OIT_TRANSMITTANCE = RenderPassLayout.simple("Flw OIT Transmittance", GpuFormat.RGBA16_FLOAT, 4, GpuFormat.D32_FLOAT);
	public static final RenderPassLayout OIT_ACCUMULATE = RenderPassLayout.simple("Flw OIT Accumulate", GpuFormat.RGBA16_FLOAT, GpuFormat.D32_FLOAT);
}
