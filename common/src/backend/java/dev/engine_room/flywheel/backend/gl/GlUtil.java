package dev.engine_room.flywheel.backend.gl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.FrameBufferCache;
import com.mojang.renderpearl.backend.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.mixin.FrontendGpuDeviceAccessor;

@Deprecated(forRemoval = true)
public class GlUtil {
	private static final MethodHandle GL_DEVICE_GET_FRAMEBUFFER_CACHE;
	private static final MethodHandle GL_DEVICE_GET_DIRECT_STATE_ACCESS;
	private static final MethodHandle GL_DEVICE_GET_STATE_MANAGER;

	private static final MethodHandle GL_BUFFER_GET_HANDLE;

	private static final MethodHandle GL_TEXTURE_GET_HANDLE;

	private static final MethodHandle GL_SAMPLER_GET_HANDLE;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();

			{
				Class<?> clazz = Class.forName("com.mojang.renderpearl.backend.opengl.GlDevice");
				MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

				{
					MethodType mt = MethodType.methodType(FrameBufferCache.class);
					GL_DEVICE_GET_FRAMEBUFFER_CACHE = privateLookup.findVirtual(clazz, "frameBufferCache", mt);
				}

				{
					MethodType mt = MethodType.methodType(DirectStateAccess.class);
					GL_DEVICE_GET_DIRECT_STATE_ACCESS = privateLookup.findVirtual(clazz, "directStateAccess", mt);
				}

				{
					MethodType mt = MethodType.methodType(GlStateManager.class);
					GL_DEVICE_GET_STATE_MANAGER = privateLookup.findVirtual(clazz, "stateManager", mt);
				}
			}

			{
				Class<?> clazz = Class.forName("com.mojang.renderpearl.backend.opengl.GlBuffer");
				MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

				MethodType mt = MethodType.methodType(int.class);
				GL_BUFFER_GET_HANDLE = privateLookup.findVirtual(clazz, "handle", mt);
			}

			{
				Class<?> clazz = Class.forName("com.mojang.renderpearl.backend.opengl.GlTexture");
				MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

				MethodType mt = MethodType.methodType(int.class);
				GL_TEXTURE_GET_HANDLE = privateLookup.findVirtual(clazz, "glId", mt);
			}

			{
				Class<?> clazz = Class.forName("com.mojang.renderpearl.backend.opengl.GlSampler");
				MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);

				MethodType mt = MethodType.methodType(int.class);
				GL_SAMPLER_GET_HANDLE = privateLookup.findVirtual(clazz, "getId", mt);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static GpuDeviceBackend getGpuDeviceBackend() {
		return ((FrontendGpuDeviceAccessor) RenderSystem.getDevice()).flywheel$getBackend();
	}

	public static FrameBufferCache getFramebufferCache() {
		try {
			return (FrameBufferCache) GL_DEVICE_GET_FRAMEBUFFER_CACHE.invoke(getGpuDeviceBackend());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static DirectStateAccess getDirectStateAccess() {
		try {
			return (DirectStateAccess) GL_DEVICE_GET_DIRECT_STATE_ACCESS.invoke(getGpuDeviceBackend());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static GlStateManager getStateManager() {
		try {
			return (GlStateManager) GL_DEVICE_GET_STATE_MANAGER.invoke(getGpuDeviceBackend());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static int getHandle(GpuBuffer buffer) {
		try {
			return (int) GL_BUFFER_GET_HANDLE.invoke(buffer);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static int getHandle(GpuTexture texture) {
		try {
			return (int) GL_TEXTURE_GET_HANDLE.invoke(texture);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static int getHandle(GpuSampler sampler) {
		try {
			return (int) GL_SAMPLER_GET_HANDLE.invoke(sampler);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
