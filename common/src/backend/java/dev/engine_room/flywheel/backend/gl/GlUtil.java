package dev.engine_room.flywheel.backend.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.FrameBufferCache;
import com.mojang.renderpearl.backend.opengl.GlDevice;

import dev.engine_room.flywheel.backend.mixin.GpuDeviceAccessor;

@Deprecated(forRemoval = true)
public class GlUtil {
	private static GlDevice getGlDevice() {
		GpuDeviceBackend backend = ((GpuDeviceAccessor) RenderSystem.getDevice()).flywheel$getBackend();
		return (GlDevice) backend;
	}

	public static FrameBufferCache getFramebufferCache() {
		return getGlDevice().frameBufferCache();
	}

	public static DirectStateAccess getDirectStateAccess() {
		return getGlDevice().directStateAccess();
	}
}
