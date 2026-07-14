package dev.engine_room.flywheel.backend.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.FrameBufferCache;

import dev.engine_room.flywheel.backend.mixin.GpuDeviceAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class GlUtil {
	private static final MethodHandle GET_FRAMEBUFFER_CACHE;
	private static final MethodHandle GET_DIRECT_STATE_ACCESS;

	static {
		try {
			Class<?> glDeviceClass = Class.forName("com.mojang.renderpearl.backend.opengl.GlDevice");
			MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(glDeviceClass, MethodHandles.lookup());

			{
				MethodType mt = MethodType.methodType(FrameBufferCache.class);
				GET_FRAMEBUFFER_CACHE = lookup.findVirtual(glDeviceClass, "frameBufferCache", mt);
			}

			{
				MethodType mt = MethodType.methodType(DirectStateAccess.class);
				GET_DIRECT_STATE_ACCESS = lookup.findVirtual(glDeviceClass, "directStateAccess", mt);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static GpuDeviceBackend getGpuDeviceBackend() {
		return ((GpuDeviceAccessor) RenderSystem.getDevice()).flywheel$getBackend();
	}

	public static FrameBufferCache getFramebufferCache() {
		try {
			return (FrameBufferCache) GET_FRAMEBUFFER_CACHE.invoke(getGpuDeviceBackend());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static DirectStateAccess getDirectStateAccess() {
		try {
			return (DirectStateAccess) GET_DIRECT_STATE_ACCESS.invoke(getGpuDeviceBackend());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
