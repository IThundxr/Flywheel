package dev.engine_room.flywheel.backend.b3d;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.mojang.blaze3d.systems.DeviceFeatures;
import com.mojang.blaze3d.systems.DeviceInfo;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;

public final class DeviceFeatureCompat {
	private static final DeviceInfo DEVICE_INFO = RenderSystem.getDevice().getDeviceInfo();
	private static final DeviceFeatures DEVICE_FEATURES = DEVICE_INFO.features();

	public static final String BACKEND_NAME = DEVICE_INFO.backendName();

	public static final boolean SUPPORTS_INSTANCING;
	public static final boolean SUPPORTS_SHADER_PARAMETERS = DEVICE_FEATURES.shaderDrawParameters();
	public static final boolean SUPPORTS_BASE_INSTANCE = DEVICE_FEATURES.nonZeroFirstInstance();
	public static final GlslVersion MAX_GLSL_VERSION;

	static {
		if (BACKEND_NAME.equals("OpenGL")) {
			GLCapabilities caps = GL.getCapabilities();

			SUPPORTS_INSTANCING = caps.OpenGL33 || caps.GL_ARB_shader_bit_encoding;
			MAX_GLSL_VERSION = GlCompat.MAX_GLSL_VERSION;
		} else if (BACKEND_NAME.equals("Vulkan")) {
			SUPPORTS_INSTANCING = true;
			// TODO - We should probably aim higher if supported
			MAX_GLSL_VERSION = GlslVersion.V450;
		} else {
			throw new RuntimeException("Unsupported rendering backend detected: " + BACKEND_NAME);
		}
	}
}
