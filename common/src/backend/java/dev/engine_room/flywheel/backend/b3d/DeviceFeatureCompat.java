package dev.engine_room.flywheel.backend.b3d;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.mojang.blaze3d.systems.DeviceFeatures;
import com.mojang.blaze3d.systems.DeviceInfo;
import com.mojang.blaze3d.systems.RenderSystem;

public final class DeviceFeatureCompat {
	private static final DeviceInfo DEVICE_INFO = RenderSystem.getDevice().getDeviceInfo();
	private static final DeviceFeatures DEVICE_FEATURES = DEVICE_INFO.features();

	public static final boolean SUPPORTS_INSTANCING;
	public static final boolean SUPPORTS_SHADER_PARAMETERS = DEVICE_FEATURES.shaderDrawParameters();
	public static final boolean SUPPORTS_BASE_INSTANCE = DEVICE_FEATURES.nonZeroFirstInstance();

	static {
		String backendName = DEVICE_INFO.backendName();

		if (backendName.equals("OpenGL")) {
			GLCapabilities caps = GL.getCapabilities();

			SUPPORTS_INSTANCING = caps.OpenGL33 || caps.GL_ARB_shader_bit_encoding;
		} else if (backendName.equals("Vulkan")) {
			SUPPORTS_INSTANCING = true;
		} else {
			throw new RuntimeException("Unsupported rendering backend detected: " + backendName);
		}
	}
}
