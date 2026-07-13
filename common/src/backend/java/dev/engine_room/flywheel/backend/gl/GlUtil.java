package dev.engine_room.flywheel.backend.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.GlDevice;

import dev.engine_room.flywheel.backend.mixin.GpuDeviceAccessor;

public class GlUtil {
	public static GlDevice getGlDevice() {
		GpuDeviceBackend backend = ((GpuDeviceAccessor) RenderSystem.getDevice()).flywheel$getBackend();
		return (GlDevice) backend;
	}
}
