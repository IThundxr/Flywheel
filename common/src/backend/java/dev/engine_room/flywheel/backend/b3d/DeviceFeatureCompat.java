package dev.engine_room.flywheel.backend.b3d;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.mojang.blaze3d.systems.DeviceInfo;
import com.mojang.blaze3d.systems.RenderSystem;

public final class DeviceFeatureCompat {
	public static final DeviceFeatureCompat INSTANCE = create();

	private final boolean supportsInstancing;

	private DeviceFeatureCompat(boolean supportsInstancing) {
		this.supportsInstancing = supportsInstancing;
	}

	public boolean isInstancingSupported() {
		return supportsInstancing;
	}

	private static DeviceFeatureCompat create() {
		DeviceInfo deviceInfo = RenderSystem.getDevice().getDeviceInfo();
		String backendName = deviceInfo.backendName();

		return switch (backendName) {
			case "OpenGL" -> createOpenGL();
			case "Vulkan" -> createVulkan();
			default -> throw new RuntimeException("Unsupported rendering backend detected: " + backendName);
		};
	}

	private static DeviceFeatureCompat createOpenGL() {
		GLCapabilities caps = GL.getCapabilities();

		boolean supportsInstancing = caps.OpenGL33 || caps.GL_ARB_shader_bit_encoding;

		return new DeviceFeatureCompat(supportsInstancing);
	}

	private static DeviceFeatureCompat createVulkan() {
		return new DeviceFeatureCompat(true);
	}
}
